from datetime import datetime
from typing import Optional
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base

class TransportVehicle(Base):
    __tablename__ = "transport_vehicles"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    owner_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    transporter_name: Mapped[str] = mapped_column(String(255), nullable=False)
    phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    vehicle_type: Mapped[str] = mapped_column(String(100), default="Truck (10 Wheeler)")
    capacity: Mapped[str] = mapped_column(String(100), default="15 Tons")
    rate_per_km: Mapped[float] = mapped_column(Float, default=45.0)
    location: Mapped[str] = mapped_column(String(255), default="Indore, MP")
    available: Mapped[bool] = mapped_column(Boolean, default=True)
    status: Mapped[str] = mapped_column(String(50), default="ACTIVE")
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    requests = relationship("TransportRequest", back_populates="vehicle", cascade="all, delete-orphan")

    def to_dict(self):
        return {
            "id": self.id,
            "owner_id": self.owner_id,
            "transporter_name": self.transporter_name,
            "phone": self.phone,
            "vehicle_type": self.vehicle_type,
            "capacity": self.capacity,
            "rate_per_km": self.rate_per_km,
            "rate": f"₹{int(self.rate_per_km) if self.rate_per_km.is_integer() else self.rate_per_km} / km",
            "location": self.location,
            "available": self.available,
            "status": self.status
        }


class TransportRequest(Base):
    __tablename__ = "transport_requests"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    vehicle_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("transport_vehicles.id"), nullable=True)
    customer_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    customer_name: Mapped[str] = mapped_column(String(255), default="Farmer")
    customer_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    
    pickup_location: Mapped[str] = mapped_column(String(255), nullable=False)
    drop_location: Mapped[str] = mapped_column(String(255), nullable=False)
    crop_type: Mapped[str] = mapped_column(String(100), default="Crop Residue")
    quantity: Mapped[str] = mapped_column(String(100), default="10 Tons")
    estimated_cost: Mapped[float] = mapped_column(Float, default=0.0)
    
    # Lifecycle: REQUESTED, ACCEPTED, PICKUP, IN_TRANSIT, DELIVERED, COMPLETED, CANCELLED
    status: Mapped[str] = mapped_column(String(50), default="REQUESTED", index=True)
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    vehicle = relationship("TransportVehicle", back_populates="requests")

    def to_dict(self):
        return {
            "id": self.id,
            "request_id": f"TR-{self.id + 1000}",
            "vehicle_id": self.vehicle_id,
            "vehicle_type": self.vehicle.vehicle_type if self.vehicle else None,
            "customer_id": self.customer_id,
            "customer_name": self.customer_name,
            "customer_phone": self.customer_phone,
            "pickup_location": self.pickup_location,
            "drop_location": self.drop_location,
            "crop_type": self.crop_type,
            "quantity": self.quantity,
            "estimated_cost": self.estimated_cost,
            "status": self.status,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }
