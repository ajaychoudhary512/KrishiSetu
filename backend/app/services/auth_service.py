"""
AgriLink AI — Auth Service

Business logic for registration, login, token refresh, OTP flows,
email verification, and password reset.
Password hashing and JWT creation are delegated to core.security.
"""
from datetime import timedelta
from typing import Optional
from uuid import UUID

from loguru import logger
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.exceptions import (
    AlreadyExistsError,
    AuthenticationError,
    BadRequestError,
    InvalidTokenError,
    NotFoundError,
)
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    hash_password,
    verify_password,
)
from app.models.otp import OTPPurpose
from app.models.user import User, UserRole
from app.repositories.otp_repository import OTPRepository
from app.repositories.user_repository import UserRepository
from app.schemas.auth import (
    LoginRequest,
    RegisterRequest,
    ResetPasswordRequest,
    TokenData,
)


class AuthService:
    def __init__(self, db: AsyncSession) -> None:
        self._db = db
        self._user_repo = UserRepository(db)
        self._otp_repo = OTPRepository(db)

    async def register(self, payload: RegisterRequest) -> User:
        """Create a new user account and dispatch welcome/verification emails."""

        if payload.email:
            existing = await self._user_repo.get_by_email(payload.email)
            if existing:
                raise AlreadyExistsError("Email")

        if payload.phone:
            existing = await self._user_repo.get_by_phone(payload.phone)
            if existing:
                raise AlreadyExistsError("Phone number")

        try:
            role = UserRole(payload.role)
        except ValueError:
            raise BadRequestError(f"Invalid role '{payload.role}'. Allowed: {[r.value for r in UserRole]}")

        user = await self._user_repo.create(
            full_name=payload.full_name,
            email=payload.email,
            phone=payload.phone,
            hashed_password=hash_password(payload.password),
            role=role,
        )

        self._dispatch_welcome_email(user)
        if payload.email:
            await self._send_email_verification(user)

        logger.info(f"[AUTH] New user registered: {user.id} role={role}")
        return user

    async def login(self, payload: LoginRequest) -> TokenData:
        """Authenticate a user and return access + refresh tokens."""

        user: Optional[User] = None
        if payload.email:
            user = await self._user_repo.get_by_email(payload.email)
        
        if not user and payload.phone:
            user = await self._user_repo.get_by_phone(payload.phone)
            if not user and not payload.phone.startswith("+"):
                user = await self._user_repo.get_by_phone(f"+91{payload.phone}")
            if not user and payload.phone.startswith("+91"):
                user = await self._user_repo.get_by_phone(payload.phone[3:])

        if not user or not user.hashed_password:
            raise AuthenticationError("Invalid credentials")

        if not verify_password(payload.password, user.hashed_password):
            raise AuthenticationError("Invalid credentials")

        if not user.is_active:
            raise AuthenticationError("Your account has been deactivated")

        return self._build_token_response(user)

    async def refresh_tokens(self, refresh_token: str) -> TokenData:
        """Issue new access + refresh tokens from a valid refresh token."""
        try:
            payload = decode_token(refresh_token)
        except Exception:
            raise InvalidTokenError()

        if payload.get("type") != "refresh":
            raise InvalidTokenError("Invalid token type")

        user_id = payload.get("sub")
        if not user_id:
            raise InvalidTokenError()

        user = await self._user_repo.get_by_id(UUID(user_id))
        if not user or user.is_deleted:
            raise AuthenticationError("User not found")

        return self._build_token_response(user)

    async def send_email_verification(self, user_id: UUID) -> None:
        user = await self._user_repo.get_by_id(user_id)
        if not user or not user.email:
            raise NotFoundError("User")
        if user.is_email_verified:
            raise BadRequestError("Email is already verified")
        await self._send_email_verification(user)

    async def verify_email(self, token: str) -> User:
        """Verify the email verification JWT token."""
        try:
            payload = decode_token(token)
        except Exception:
            raise InvalidTokenError("Invalid or expired verification link")

        if payload.get("purpose") != "email_verify":
            raise InvalidTokenError("Invalid token purpose")

        user_id = payload.get("sub")
        user = await self._user_repo.get_by_id(UUID(user_id))
        if not user:
            raise NotFoundError("User")

        if not user.is_email_verified:
            await self._user_repo.update(user, is_email_verified=True)

        return user

    async def forgot_password(self, email: str) -> None:
        """Send a password-reset link (silently if email not found, to avoid enumeration)."""
        user = await self._user_repo.get_by_email(email)
        if not user:
            return  # Silent — don't reveal if email exists

        token = create_access_token(
            subject=str(user.id),
            extra_claims={"purpose": "password_reset"},
            expires_delta=timedelta(hours=1),
        )
        try:
            from app.workers.tasks.email_tasks import send_password_reset_email
            send_password_reset_email.delay(user.email, user.full_name, token)
        except Exception as exc:
            logger.warning(f"[AUTH] Could not dispatch password reset email: {exc}")

    async def reset_password(self, payload: ResetPasswordRequest) -> None:
        """Validate the reset token and update the password."""
        try:
            data = decode_token(payload.token)
        except Exception:
            raise InvalidTokenError("Invalid or expired reset link")

        if data.get("purpose") != "password_reset":
            raise InvalidTokenError("Invalid token purpose")

        user = await self._user_repo.get_by_id(UUID(data["sub"]))
        if not user:
            raise NotFoundError("User")

        await self._user_repo.update(
            user, hashed_password=hash_password(payload.new_password)
        )

    async def change_password(
        self, user: User, current_password: str, new_password: str
    ) -> None:
        """Change password for an authenticated user."""
        if not user.hashed_password or not verify_password(current_password, user.hashed_password):
            raise AuthenticationError("Current password is incorrect")
        await self._user_repo.update(user, hashed_password=hash_password(new_password))

    async def send_otp(self, phone: str, purpose: OTPPurpose) -> None:
        user = await self._user_repo.get_by_phone(phone)
        if not user:
            raise NotFoundError("Phone number not registered")
        otp = await self._otp_repo.create_otp(user.id, purpose)
        try:
            from app.workers.tasks.notification_tasks import send_sms
            send_sms.delay(phone, f"Your AgriLink OTP is {otp.code}. Valid for 10 minutes.")
        except Exception as exc:
            logger.warning(f"[AUTH] Could not dispatch SMS task: {exc}")

    async def verify_otp(self, phone: str, code: str, purpose: OTPPurpose) -> TokenData:
        user = await self._user_repo.get_by_phone(phone)
        if not user:
            raise AuthenticationError("Invalid OTP")

        otp = await self._otp_repo.get_valid_otp(user.id, code, purpose)
        if not otp:
            raise AuthenticationError("Invalid or expired OTP")

        await self._otp_repo.mark_used(otp)

        if purpose == OTPPurpose.PHONE_VERIFY and not user.is_phone_verified:
            await self._user_repo.update(user, is_phone_verified=True)

        return self._build_token_response(user)

    def _build_token_response(self, user: User) -> TokenData:
        extra = {"role": user.role.value if hasattr(user.role, "value") else str(user.role), "email": user.email}
        access_token = create_access_token(subject=str(user.id), extra_claims=extra)
        refresh_token = create_refresh_token(subject=str(user.id))
        return TokenData(
            access_token=access_token,
            refresh_token=refresh_token,
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        )

    async def _send_email_verification(self, user: User) -> None:
        try:
            token = create_access_token(
                subject=str(user.id),
                extra_claims={"purpose": "email_verify"},
                expires_delta=timedelta(hours=24),
            )
            from app.workers.tasks.email_tasks import send_verification_email
            send_verification_email.delay(user.email, user.full_name, token)
        except Exception as exc:
            logger.warning(f"[AUTH] Could not dispatch email verification task: {exc}")

    def _dispatch_welcome_email(self, user: User) -> None:
        if user.email:
            try:
                from app.workers.tasks.email_tasks import send_welcome_email
                send_welcome_email.delay(user.email, user.full_name)
            except Exception as exc:
                logger.warning(f"[AUTH] Could not dispatch welcome email task: {exc}")
