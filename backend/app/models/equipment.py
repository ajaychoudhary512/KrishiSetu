from datetime import datetime
from typing import Optional
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base

class EquipmentListing(Base):
    __tablename__ = "equipment_listings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    owner_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    owner_name: Mapped[str] = mapped_column(String(255), default="Equipment Owner")
    owner_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    category: Mapped[str] = mapped_column(String(100), default="Tractor")
    source_type: Mapped[str] = mapped_column(String(50), default="farmer")
    rate: Mapped[float] = mapped_column(Float, nullable=False)
    rate_unit: Mapped[str] = mapped_column(String(50), default="Hr")
    location: Mapped[str] = mapped_column(String(255), nullable=False)
    rating: Mapped[float] = mapped_column(Float, default=4.8)
    image_url: Mapped[str] = mapped_column(Text, default="assets/agri_waste_banner.png")
    available: Mapped[bool] = mapped_column(Boolean, default=True)
    status: Mapped[str] = mapped_column(String(50), default="ACTIVE")
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    bookings = relationship("EquipmentBooking", back_populates="equipment", cascade="all, delete-orphan")

    def to_dict(self):
        return {
            "id": self.id,
            "owner_id": self.owner_id,
            "owner": self.owner_name,
            "owner_phone": self.owner_phone,
            "name": self.name,
            "category": self.category,
            "source_type": self.source_type,
            "rate": f"₹{int(self.rate) if self.rate.is_integer() else self.rate} / {self.rate_unit}",
            "raw_rate": self.rate,
            "rate_unit": self.rate_unit,
            "location": self.location,
            "rating": self.rating,
            "image_url": self.image_url,
            "available": self.available,
            "status": self.status
        }


class EquipmentBooking(Base):
    __tablename__ = "equipment_bookings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    equipment_id: Mapped[int] = mapped_column(Integer, ForeignKey("equipment_listings.id"), nullable=False)
    renter_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    renter_name: Mapped[str] = mapped_column(String(255), default="Farmer")
    renter_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    
    days: Mapped[int] = mapped_column(Integer, default=1)
    start_date: Mapped[str] = mapped_column(String(50), nullable=False)
    total_cost: Mapped[float] = mapped_column(Float, default=0.0)
    status: Mapped[str] = mapped_column(String(50), default="REQUESTED")  # REQUESTED, ACCEPTED, REJECTED, ACTIVE, COMPLETED, CANCELLED
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    equipment = relationship("EquipmentListing", back_populates="bookings")

    def to_dict(self):
        return {
            "id": self.id,
            "booking_id": f"EQ-{self.id + 89400}",
            "equipment_id": self.equipment_id,
            "equipment_name": self.equipment.name if self.equipment else None,
            "renter_id": self.renter_id,
            "renter_name": self.renter_name,
            "renter_phone": self.renter_phone,
            "days": self.days,
            "start_date": self.start_date,
            "total_cost": self.total_cost,
            "status": self.status,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }
