"""
AgriLink AI — Auth API Endpoints

POST /auth/register           → Create new account
POST /auth/login              → Email/Phone + password login
POST /auth/refresh            → Refresh access token
POST /auth/logout             → Client-side token discard (stateless)
POST /auth/send-otp           → Send OTP to phone
POST /auth/verify-otp         → Verify OTP → return tokens
GET  /auth/verify-email       → Verify email via link token
POST /auth/resend-verification → Resend email verification
POST /auth/forgot-password    → Send reset link
POST /auth/reset-password     → Reset with token
POST /auth/change-password    → Authenticated password change
"""
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.database.session import get_db
from app.models.otp import OTPPurpose
from app.schemas.auth import (
    ChangePasswordRequest,
    ForgotPasswordRequest,
    LoginRequest,
    RefreshTokenRequest,
    RegisterRequest,
    ResetPasswordRequest,
    SendOTPRequest,
    VerifyEmailRequest,
    VerifyOTPRequest,
)
from app.schemas.user import UserOut
from app.services.auth_service import AuthService
from app.utils.response import success_response

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post(
    "/register",
    status_code=status.HTTP_201_CREATED,
    summary="Register a new user",
    response_description="User created successfully",
)
async def register(payload: RegisterRequest, db: AsyncSession = Depends(get_db)):
    svc = AuthService(db)
    user = await svc.register(payload)
    return success_response(
        data=UserOut.model_validate(user).model_dump(mode="json"),
        message="Account created successfully. Please verify your email.",
        status_code=201,
    )


@router.post("/login", summary="Login with email/phone + password")
async def login(payload: LoginRequest, db: AsyncSession = Depends(get_db)):
    svc = AuthService(db)
    tokens = await svc.login(payload)
    return success_response(data=tokens.model_dump(mode="json"), message="Login successful")


@router.post("/refresh", summary="Refresh access token")
async def refresh(payload: RefreshTokenRequest, db: AsyncSession = Depends(get_db)):
    svc = AuthService(db)
    tokens = await svc.refresh_tokens(payload.refresh_token)
    return success_response(data=tokens.model_dump(mode="json"), message="Tokens refreshed")


@router.post("/logout", summary="Logout (stateless — discard tokens client-side)")
async def logout():
    """
    Since this is a stateless JWT setup the server doesn't track tokens.
    Clients should discard the tokens locally on logout.
    For token blacklisting, integrate Redis-backed token revocation here.
    """
    return success_response(message="Logged out successfully")


@router.post("/send-otp", summary="Send OTP to a registered phone number")
async def send_otp(payload: SendOTPRequest, db: AsyncSession = Depends(get_db)):
    svc = AuthService(db)
    purpose = OTPPurpose(payload.purpose)
    await svc.send_otp(payload.phone, purpose)
    return success_response(message="OTP sent successfully")


@router.post("/verify-otp", summary="Verify phone OTP and get tokens")
async def verify_otp(payload: VerifyOTPRequest, db: AsyncSession = Depends(get_db)):
    svc = AuthService(db)
    purpose = OTPPurpose(payload.purpose)
    tokens = await svc.verify_otp(payload.phone, payload.code, purpose)
    return success_response(data=tokens.model_dump(mode="json"), message="OTP verified. Login successful.")


@router.get("/verify-email", summary="Verify email via link token")
async def verify_email(token: str, db: AsyncSession = Depends(get_db)):
    svc = AuthService(db)
    user = await svc.verify_email(token)
    return success_response(
        data=UserOut.model_validate(user).model_dump(mode="json"),
        message="Email verified successfully",
    )


@router.post("/resend-verification", summary="Resend email verification link")
async def resend_verification(
    db: AsyncSession = Depends(get_db),
    current_user=Depends(get_current_user),
):
    svc = AuthService(db)
    await svc.send_email_verification(current_user.id)
    return success_response(message="Verification email sent")


@router.post("/forgot-password", summary="Request a password reset link")
async def forgot_password(
    payload: ForgotPasswordRequest, db: AsyncSession = Depends(get_db)
):
    svc = AuthService(db)
    await svc.forgot_password(payload.email)
    return success_response(
        message="If that email is registered, a reset link has been sent."
    )


@router.post("/reset-password", summary="Reset password using token from email")
async def reset_password(
    payload: ResetPasswordRequest, db: AsyncSession = Depends(get_db)
):
    svc = AuthService(db)
    await svc.reset_password(payload)
    return success_response(message="Password reset successfully")


@router.post("/change-password", summary="Change password for authenticated user")
async def change_password(
    payload: ChangePasswordRequest,
    db: AsyncSession = Depends(get_db),
    current_user=Depends(get_current_user),
):
    svc = AuthService(db)
    await svc.change_password(current_user, payload.current_password, payload.new_password)
    return success_response(message="Password changed successfully")
