import secrets
from datetime import datetime, timedelta, timezone
from typing import Optional
from uuid import UUID

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.otp import OTPCode, OTPPurpose

OTP_TTL_MINUTES = 10

class OTPRepository:
    

    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    def _generate_code(self) -> str:
        
        return str(secrets.randbelow(900000) + 100000)

    async def create_otp(self, user_id: UUID, purpose: OTPPurpose) -> OTPCode:
        
        await self.db.execute(
            update(OTPCode)
            .where(OTPCode.user_id == user_id, OTPCode.purpose == purpose, OTPCode.is_used.is_(False))
            .values(is_used=True)
        )
        otp = OTPCode(
            user_id=user_id,
            code=self._generate_code(),
            purpose=purpose,
            expires_at=datetime.now(timezone.utc) + timedelta(minutes=OTP_TTL_MINUTES),
        )
        self.db.add(otp)
        await self.db.flush()
        await self.db.refresh(otp)
        return otp

    async def get_valid_otp(
        self, user_id: UUID, code: str, purpose: OTPPurpose
    ) -> Optional[OTPCode]:
        
        now = datetime.now(timezone.utc)
        result = await self.db.execute(
            select(OTPCode).where(
                OTPCode.user_id == user_id,
                OTPCode.code == code,
                OTPCode.purpose == purpose,
                OTPCode.is_used.is_(False),
                OTPCode.expires_at > now,
            )
        )
        return result.scalar_one_or_none()

    async def mark_used(self, otp: OTPCode) -> None:
        otp.is_used = True
        self.db.add(otp)
        await self.db.flush()
