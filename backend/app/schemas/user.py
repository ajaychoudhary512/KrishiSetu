"""
AgriLink AI — User Pydantic Schemas

Defines request/response shapes for user profile operations.
"""
from typing import Optional
from uuid import UUID
from datetime import datetime

from pydantic import BaseModel, EmailStr, Field


# ── User output (safe — no password) ─────────────────────────────────────────
class UserOut(BaseModel):
    id: UUID
    full_name: str
    email: Optional[str] = None
    phone: Optional[str] = None
    role: str
    is_email_verified: bool
    is_phone_verified: bool
    is_active: bool
    avatar_url: Optional[str] = None
    bio: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    pincode: Optional[str] = None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


# ── Update profile ────────────────────────────────────────────────────────────
class UserUpdateRequest(BaseModel):
    full_name: Optional[str] = Field(None, min_length=2, max_length=255)
    bio: Optional[str] = Field(None, max_length=500)
    city: Optional[str] = Field(None, max_length=100)
    state: Optional[str] = Field(None, max_length=100)
    pincode: Optional[str] = Field(None, pattern=r"^\d{6}$")
