"""
AgriLink AI — Abstract Storage Service (AWS S3 / Local)

Provides a unified interface for file storage. Can be backed by
AWS S3, local filesystem, or any other provider.
"""
import os
import uuid
from abc import ABC, abstractmethod
from typing import Optional

from app.core.config import settings


class StorageService(ABC):
    """Abstract base class for file storage providers."""

    @abstractmethod
    async def upload_file(
        self,
        file_data: bytes,
        filename: str,
        content_type: str,
        folder: str = "uploads",
    ) -> str:
        """Upload a file and return its public URL."""
        ...

    @abstractmethod
    async def delete_file(self, file_url: str) -> bool:
        """Delete a file by its URL. Returns True on success."""
        ...

    @abstractmethod
    async def get_presigned_url(self, file_key: str, expires_in: int = 3600) -> str:
        """Get a presigned URL for private file access."""
        ...


class S3StorageService(StorageService):
    """AWS S3 storage backend."""

    def __init__(self):
        import boto3
        self._client = boto3.client(
            "s3",
            aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
            aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
            region_name=settings.AWS_REGION,
        )
        self._bucket = settings.AWS_S3_BUCKET

    async def upload_file(
        self,
        file_data: bytes,
        filename: str,
        content_type: str,
        folder: str = "uploads",
    ) -> str:
        """Upload file to S3 and return public URL."""
        ext = filename.rsplit(".", 1)[-1] if "." in filename else "bin"
        key = f"{folder}/{uuid.uuid4()}.{ext}"
        self._client.put_object(
            Bucket=self._bucket,
            Key=key,
            Body=file_data,
            ContentType=content_type,
        )
        return f"{settings.AWS_S3_BASE_URL}/{key}"

    async def delete_file(self, file_url: str) -> bool:
        key = file_url.replace(f"{settings.AWS_S3_BASE_URL}/", "")
        self._client.delete_object(Bucket=self._bucket, Key=key)
        return True

    async def get_presigned_url(self, file_key: str, expires_in: int = 3600) -> str:
        return self._client.generate_presigned_url(
            "get_object",
            Params={"Bucket": self._bucket, "Key": file_key},
            ExpiresIn=expires_in,
        )


class LocalStorageService(StorageService):
    """Local filesystem storage backend (development use only)."""

    def __init__(self, base_dir: str = "media"):
        self._base_dir = base_dir
        os.makedirs(base_dir, exist_ok=True)

    async def upload_file(
        self,
        file_data: bytes,
        filename: str,
        content_type: str,
        folder: str = "uploads",
    ) -> str:
        folder_path = os.path.join(self._base_dir, folder)
        os.makedirs(folder_path, exist_ok=True)
        ext = filename.rsplit(".", 1)[-1] if "." in filename else "bin"
        new_filename = f"{uuid.uuid4()}.{ext}"
        file_path = os.path.join(folder_path, new_filename)
        with open(file_path, "wb") as f:
            f.write(file_data)
        return f"/media/{folder}/{new_filename}"

    async def delete_file(self, file_url: str) -> bool:
        path = file_url.lstrip("/")
        if os.path.exists(path):
            os.remove(path)
        return True

    async def get_presigned_url(self, file_key: str, expires_in: int = 3600) -> str:
        return f"/media/{file_key}"


def get_storage_service() -> StorageService:
    """Factory: return the configured storage service."""
    if settings.AWS_ACCESS_KEY_ID and settings.ENVIRONMENT == "production":
        return S3StorageService()
    return LocalStorageService()


storage_service: StorageService = get_storage_service()
