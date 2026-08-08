"""
AgriLink AI — User Profile API Endpoints

GET    /users/me           → Get own profile
PUT    /users/me           → Update own profile
DELETE /users/me           → Soft-delete account
POST   /users/me/avatar    → Upload profile avatar
"""
from fastapi import APIRouter, Depends, File, UploadFile, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.core.exceptions import BadRequestError
from app.database.session import get_db
from app.schemas.user import UserOut, UserUpdateRequest
from app.services.user_service import UserService
from app.utils.response import success_response
from app.utils.s3 import storage_service

router = APIRouter(prefix="/users", tags=["Users"])

ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"}
MAX_AVATAR_SIZE = 5 * 1024 * 1024


@router.get("/me", summary="Get my profile", response_description="Current user profile")
async def get_me(current_user=Depends(get_current_user)):
    return success_response(
        data=UserOut.model_validate(current_user).model_dump(),
        message="Profile fetched successfully",
    )


@router.put("/me", summary="Update my profile")
async def update_me(
    payload: UserUpdateRequest,
    db: AsyncSession = Depends(get_db),
    current_user=Depends(get_current_user),
):
    svc = UserService(db)
    user = await svc.update_profile(current_user, payload)
    return success_response(
        data=UserOut.model_validate(user).model_dump(),
        message="Profile updated successfully",
    )


@router.delete("/me", status_code=status.HTTP_200_OK, summary="Delete my account")
async def delete_me(
    db: AsyncSession = Depends(get_db),
    current_user=Depends(get_current_user),
):
    svc = UserService(db)
    await svc.delete_account(current_user)
    return success_response(message="Account deleted successfully")


@router.post("/me/avatar", summary="Upload profile avatar")
async def upload_avatar(
    file: UploadFile = File(..., description="JPEG/PNG/WebP image, max 5MB"),
    db: AsyncSession = Depends(get_db),
    current_user=Depends(get_current_user),
):
    if file.content_type not in ALLOWED_IMAGE_TYPES:
        raise BadRequestError(f"Unsupported file type '{file.content_type}'. Use JPEG, PNG, or WebP.")

    contents = await file.read()
    if len(contents) > MAX_AVATAR_SIZE:
        raise BadRequestError("File too large. Maximum avatar size is 5 MB.")

    url = await storage_service.upload_file(
        file_data=contents,
        filename=file.filename or "avatar",
        content_type=file.content_type,
        folder="avatars",
    )

    svc = UserService(db)
    user = await svc.update_profile(current_user, UserUpdateRequest())
    from app.repositories.user_repository import UserRepository
    repo = UserRepository(db)
    user = await repo.update(current_user, avatar_url=url)

    return success_response(
        data={"avatar_url": url},
        message="Avatar uploaded successfully",
    )
