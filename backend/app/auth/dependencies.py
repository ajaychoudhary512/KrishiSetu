from typing import List
from uuid import UUID

from fastapi import Depends, Header, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import AuthenticationError, InvalidTokenError, PermissionDeniedError
from app.core.security import decode_token
from app.database.session import get_db

security = HTTPBearer(auto_error=False)

async def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> UUID:
    
    if not credentials:
        raise AuthenticationError("No authentication token provided")

    try:
        payload = decode_token(credentials.credentials)
    except JWTError:
        raise InvalidTokenError()

    if payload.get("type") != "access":
        raise InvalidTokenError("Invalid token type")

    user_id = payload.get("sub")
    if not user_id:
        raise InvalidTokenError("Token missing subject claim")

    return UUID(user_id)

async def get_current_user(
    user_id: UUID = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    
    from app.repositories.user_repository import UserRepository

    repo = UserRepository(db)
    user = await repo.get_by_id(user_id)
    if not user or user.is_deleted:
        raise AuthenticationError("User account not found or has been deactivated")
    return user

def require_roles(*roles: str):
    
    async def _check_role(current_user=Depends(get_current_user)):
        if current_user.role.value not in roles:
            raise PermissionDeniedError(
                f"This action requires one of these roles: {', '.join(roles)}"
            )
        return current_user

    return _check_role
