"""
AgriLink AI — SQLAlchemy Declarative Base

Provides a base class with common audit fields (created_at, updated_at, deleted_at)
and a soft-delete mixin for all ORM models.
"""
import uuid
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import DateTime, func, text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    """Declarative base for all AgriLink AI ORM models."""
    pass


class TimestampMixin:
    """Mixin that adds created_at and updated_at audit fields."""

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        index=True,
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )


class SoftDeleteMixin:
    """Mixin that adds soft-delete support via deleted_at timestamp."""

    deleted_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
        default=None,
        index=True,
    )

    @property
    def is_deleted(self) -> bool:
        return self.deleted_at is not None

    def soft_delete(self) -> None:
        """Mark record as deleted without removing from database."""
        self.deleted_at = datetime.now(timezone.utc)


class UUIDMixin:
    """Mixin that adds a UUID primary key."""

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        server_default=text("gen_random_uuid()"),
    )


class AuditMixin(UUIDMixin, TimestampMixin, SoftDeleteMixin):
    """Full audit mixin: UUID PK + timestamps + soft delete."""
    pass
