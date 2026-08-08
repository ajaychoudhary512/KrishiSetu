from typing import Optional
from uuid import UUID

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User, UserRole

class UserRepository:
    

    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_by_id(self, user_id: UUID) -> Optional[User]:
        result = await self.db.execute(
            select(User).where(User.id == user_id, User.deleted_at.is_(None))
        )
        return result.scalar_one_or_none()

    async def get_by_email(self, email: str) -> Optional[User]:
        result = await self.db.execute(
            select(User).where(User.email == email, User.deleted_at.is_(None))
        )
        return result.scalar_one_or_none()

    async def get_by_phone(self, phone: str) -> Optional[User]:
        result = await self.db.execute(
            select(User).where(User.phone == phone, User.deleted_at.is_(None))
        )
        return result.scalar_one_or_none()

    async def get_by_google_id(self, google_id: str) -> Optional[User]:
        result = await self.db.execute(
            select(User).where(User.google_id == google_id, User.deleted_at.is_(None))
        )
        return result.scalar_one_or_none()

    async def create(
        self,
        *,
        full_name: str,
        role: UserRole,
        email: Optional[str] = None,
        phone: Optional[str] = None,
        hashed_password: Optional[str] = None,
        google_id: Optional[str] = None,
        is_email_verified: bool = False,
        is_phone_verified: bool = False,
    ) -> User:
        user = User(
            full_name=full_name,
            email=email,
            phone=phone,
            hashed_password=hashed_password,
            role=role,
            google_id=google_id,
            is_email_verified=is_email_verified,
            is_phone_verified=is_phone_verified,
        )
        self.db.add(user)
        await self.db.flush()
        await self.db.refresh(user)
        return user

    async def update(self, user: User, **kwargs) -> User:
        for key, value in kwargs.items():
            setattr(user, key, value)
        self.db.add(user)
        await self.db.flush()
        await self.db.refresh(user)
        return user

    async def soft_delete(self, user: User) -> None:
        user.soft_delete()
        self.db.add(user)
        await self.db.flush()
