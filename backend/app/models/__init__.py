"""
AgriLink AI — ORM Models

Import all models here so that Alembic's autogenerate can detect them.
"""
from app.models.user import User  # noqa: F401
from app.models.otp import OTPCode  # noqa: F401
