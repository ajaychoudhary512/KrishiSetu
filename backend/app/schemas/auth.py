"""
AgriLink AI — Auth Pydantic Schemas

Request/Response schemas for all authentication endpoints.
All passwords are validated for minimum strength.
"""
import re
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, EmailStr, Field, field_validator, model_validator


PASSWORD_PATTERN = re.compile(
    r"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_\-])[A-Za-z\d@$!%*?&_\-]{8,}$"
)


def _validate_password(v: str) -> str:
    if len(v) < 6:
        raise ValueError("Password must be at least 6 characters long")
    return v


class RegisterRequest(BaseModel):
    full_name: str = Field(..., min_length=2, max_length=255, examples=["Ravi Kumar"])
    email: Optional[EmailStr] = Field(None, examples=["ravi@example.com"])
    phone: Optional[str] = Field(None, pattern=r"^\+?[1-9]\d{9,14}$", examples=["+919876543210"])
    password: str = Field(..., min_length=8, examples=["Secure@123"])
    role: str = Field("farmer", examples=["farmer"])

    @field_validator("email", "phone", mode="before")
    @classmethod
    def empty_str_to_none(cls, v: Optional[str]) -> Optional[str]:
        if v == "":
            return None
        return v

    @field_validator("password")
    @classmethod
    def validate_password(cls, v: str) -> str:
        return _validate_password(v)

    @model_validator(mode="after")
    def email_or_phone_required(self) -> "RegisterRequest":
        if not self.email and not self.phone:
            raise ValueError("Either email or phone is required")
        return self


class LoginRequest(BaseModel):
    username: Optional[str] = Field(None, examples=["ravi@example.com"])
    email: Optional[str] = Field(None, examples=["ravi@example.com"])
    phone: Optional[str] = Field(None, examples=["+919876543210"])
    password: str = Field(..., examples=["Secure@123"])

    @model_validator(mode="after")
    def email_or_phone_required(self) -> "LoginRequest":
        if self.username:
            if "@" in self.username:
                self.email = self.username
            else:
                self.phone = self.username
        if not self.email and not self.phone:
            raise ValueError("Either email, phone, or username is required")
        return self



class TokenData(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int


class RefreshTokenRequest(BaseModel):
    refresh_token: str


class SendOTPRequest(BaseModel):
    phone: str = Field(..., pattern=r"^\+?[1-9]\d{9,14}$")
    purpose: str = Field("login", examples=["login", "phone_verify"])


class VerifyOTPRequest(BaseModel):
    phone: str
    code: str = Field(..., min_length=6, max_length=6)
    purpose: str = Field("login")


class VerifyEmailRequest(BaseModel):
    token: str


class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ResetPasswordRequest(BaseModel):
    token: str
    new_password: str = Field(..., min_length=8)
    confirm_password: str

    @field_validator("new_password")
    @classmethod
    def validate_password(cls, v: str) -> str:
        return _validate_password(v)

    @model_validator(mode="after")
    def passwords_match(self) -> "ResetPasswordRequest":
        if self.new_password != self.confirm_password:
            raise ValueError("Passwords do not match")
        return self


class ChangePasswordRequest(BaseModel):
    current_password: str
    new_password: str = Field(..., min_length=8)
    confirm_password: str

    @field_validator("new_password")
    @classmethod
    def validate_password(cls, v: str) -> str:
        return _validate_password(v)

    @model_validator(mode="after")
    def passwords_match(self) -> "ChangePasswordRequest":
        if self.new_password != self.confirm_password:
            raise ValueError("Passwords do not match")
        return self
