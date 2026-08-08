"""
AgriLink AI — User ORM Model

Defines the User table with role-based access control, phone/email
authentication support, soft-delete, and profile fields.
"""
import enum
import uuid

from sqlalchemy import Boolean, Enum, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import AuditMixin, Base


class UserRole(str, enum.Enum):
    """Roles supported by the platform."""
    FARMER = "farmer"
    INDUSTRY = "industry"
    LABOR = "labor"
    TRANSPORT = "transport"
    ADMIN = "admin"


class User(AuditMixin, Base):
    """
    Platform user model.

    Supports both email/password and phone/OTP login flows.
    Role determines which marketplace features are accessible.
    """
    __tablename__ = "users"

    full_name: Mapped[str] = mapped_column(String(255), nullable=False)
    email: Mapped[str | None] = mapped_column(
        String(320), unique=True, nullable=True, index=True
    )
    phone: Mapped[str | None] = mapped_column(
        String(20), unique=True, nullable=True, index=True
    )

    hashed_password: Mapped[str | None] = mapped_column(Text, nullable=True)
    role: Mapped[UserRole] = mapped_column(
        Enum(UserRole, name="userrole"), nullable=False, default=UserRole.FARMER
    )

    is_email_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_phone_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    avatar_url: Mapped[str | None] = mapped_column(Text, nullable=True)
    bio: Mapped[str | None] = mapped_column(Text, nullable=True)
    city: Mapped[str | None] = mapped_column(String(100), nullable=True)
    state: Mapped[str | None] = mapped_column(String(100), nullable=True)
    pincode: Mapped[str | None] = mapped_column(String(10), nullable=True)

    google_id: Mapped[str | None] = mapped_column(String(255), unique=True, nullable=True)

    def __repr__(self) -> str:
        return f"<User id={self.id} email={self.email} role={self.role}>"
