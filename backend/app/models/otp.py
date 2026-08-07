"""
AgriLink AI — OTP Code ORM Model

Stores one-time passwords for phone verification and email verification.
Each OTP expires after a configurable TTL and is invalidated after first use.
"""
import enum
import uuid
from datetime import datetime

from sqlalchemy import Boolean, DateTime, Enum, ForeignKey, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin, UUIDMixin


class OTPPurpose(str, enum.Enum):
    EMAIL_VERIFY = "email_verify"
    PHONE_VERIFY = "phone_verify"
    PASSWORD_RESET = "password_reset"
    LOGIN = "login"


class OTPCode(UUIDMixin, TimestampMixin, Base):
    """
    One-time password record tied to a user and purpose.
    """
    __tablename__ = "otp_codes"

    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    code: Mapped[str] = mapped_column(String(6), nullable=False)
    purpose: Mapped[OTPPurpose] = mapped_column(
        Enum(OTPPurpose, name="otppurpose"), nullable=False
    )
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    is_used: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    # Relationship
    user = relationship("User", backref="otp_codes", lazy="select")

    def __repr__(self) -> str:
        return f"<OTPCode user={self.user_id} purpose={self.purpose} used={self.is_used}>"
