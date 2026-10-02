from datetime import datetime
from typing import Optional
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base

class LaborJob(Base):
    __tablename__ = "labor_jobs"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    posted_by_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    posted_by: Mapped[str] = mapped_column(String(255), default="Farmer")
    posted_by_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    source_type: Mapped[str] = mapped_column(String(50), default="farmer")
    workers_needed: Mapped[int] = mapped_column(Integer, default=1)
    wage: Mapped[str] = mapped_column(String(100), nullable=False)
    location: Mapped[str] = mapped_column(String(255), nullable=False)
    crop_type: Mapped[str] = mapped_column(String(100), default="General Farm")
    status: Mapped[str] = mapped_column(String(50), default="ACTIVE")
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    applications = relationship("LaborApplication", back_populates="job", cascade="all, delete-orphan")

    def to_dict(self):
        return {
            "id": self.id,
            "posted_by_id": self.posted_by_id,
            "posted_by": self.posted_by,
            "title": self.title,
            "source_type": self.source_type,
            "workers_needed": self.workers_needed,
            "wage": self.wage,
            "location": self.location,
            "crop_type": self.crop_type,
            "status": self.status,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }


class WorkerProfile(Base):
    __tablename__ = "worker_profiles"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[Optional[str]] = mapped_column(String(64), unique=True, index=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    skills: Mapped[str] = mapped_column(Text, default="Harvesting, Sowing, Tilling")
    experience: Mapped[str] = mapped_column(String(100), default="5+ Years")
    location: Mapped[str] = mapped_column(String(255), default="Indore, MP")
    daily_rate: Mapped[float] = mapped_column(Float, default=500.0)
    is_available: Mapped[bool] = mapped_column(Boolean, default=True)
    completed_jobs: Mapped[int] = mapped_column(Integer, default=0)
    rating: Mapped[float] = mapped_column(Float, default=4.9)
    
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False
    )

    def to_dict(self):
        return {
            "id": self.id,
            "user_id": self.user_id,
            "name": self.name,
            "phone": self.phone,
            "skills": self.skills,
            "experience": self.experience,
            "location": self.location,
            "daily_rate": self.daily_rate,
            "is_available": self.is_available,
            "availability": "Available" if self.is_available else "Unavailable",
            "completed_jobs": self.completed_jobs,
            "rating": self.rating
        }


class LaborApplication(Base):
    __tablename__ = "labor_applications"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    job_id: Mapped[int] = mapped_column(Integer, ForeignKey("labor_jobs.id"), nullable=False)
    worker_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    worker_name: Mapped[str] = mapped_column(String(255), default="Worker")
    worker_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    status: Mapped[str] = mapped_column(String(50), default="PENDING")  # PENDING, ACCEPTED, REJECTED, COMPLETED
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    job = relationship("LaborJob", back_populates="applications")

    def to_dict(self):
        return {
            "id": self.id,
            "job_id": self.job_id,
            "job_title": self.job.title if self.job else None,
            "worker_id": self.worker_id,
            "worker_name": self.worker_name,
            "worker_phone": self.worker_phone,
            "status": self.status,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }
