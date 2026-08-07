"""
AgriLink AI — User Service

Business logic for user profile management.
"""
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundError
from app.models.user import User
from app.repositories.user_repository import UserRepository
from app.schemas.user import UserUpdateRequest


class UserService:
    def __init__(self, db: AsyncSession) -> None:
        self._repo = UserRepository(db)

    async def get_profile(self, user_id: UUID) -> User:
        user = await self._repo.get_by_id(user_id)
        if not user:
            raise NotFoundError("User")
        return user

    async def update_profile(self, user: User, payload: UserUpdateRequest) -> User:
        """Update mutable profile fields."""
        updates = payload.model_dump(exclude_unset=True)
        return await self._repo.update(user, **updates)

    async def delete_account(self, user: User) -> None:
        """Soft-delete the user account."""
        await self._repo.soft_delete(user)
