import uuid
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base

class WasteListing(Base):
    __tablename__ = "waste_listings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    seller_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    seller_name: Mapped[str] = mapped_column(String(255), default="Farmer")
    seller_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    category: Mapped[str] = mapped_column(String(100), default="Crop Residue")
    source_type: Mapped[str] = mapped_column(String(50), default="farmer")  # "farmer" or "industry"
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    
    original_quantity: Mapped[float] = mapped_column(Float, nullable=False)
    remaining_quantity: Mapped[float] = mapped_column(Float, nullable=False)
    unit: Mapped[str] = mapped_column(String(50), default="Quintal")
    
    price: Mapped[float] = mapped_column(Float, nullable=False)
    price_unit: Mapped[str] = mapped_column(String(50), default="Quintal")
    
    location: Mapped[str] = mapped_column(String(255), nullable=False)
    image_url: Mapped[str] = mapped_column(Text, default="rice_straw")
    
    # Lifecycle states: ACTIVE, PARTIALLY_SOLD, SOLD_OUT, PAUSED, CANCELLED
    status: Mapped[str] = mapped_column(String(50), default="ACTIVE", index=True)
    is_verified: Mapped[bool] = mapped_column(Boolean, default=True)
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False
    )

    orders = relationship("WasteOrder", back_populates="listing", cascade="all, delete-orphan")

    def to_dict(self):
        return {
            "id": self.id,
            "seller_id": self.seller_id,
            "seller_name": self.seller_name,
            "seller_phone": self.seller_phone,
            "title": self.title,
            "category": self.category,
            "source_type": self.source_type,
            "description": self.description or "",
            "original_quantity": self.original_quantity,
            "remaining_quantity": self.remaining_quantity,
            "unit": self.unit,
            "price": self.price,
            "price_per_unit": f"₹{int(self.price) if self.price.is_integer() else self.price}/{self.price_unit.lower()}",
            "price_unit": self.price_unit,
            "location": self.location,
            "location_name": self.location,
            "image_url": self.image_url,
            "status": self.status,
            "is_verified": self.is_verified,
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None,
            "farmer_name": self.seller_name,
            "quantity": f"{int(self.remaining_quantity) if self.remaining_quantity.is_integer() else self.remaining_quantity} {self.unit}"
        }


class WasteOrder(Base):
    __tablename__ = "waste_orders"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    order_number: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    listing_id: Mapped[int] = mapped_column(Integer, ForeignKey("waste_listings.id"), nullable=False)
    
    buyer_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    buyer_name: Mapped[str] = mapped_column(String(255), default="Buyer")
    buyer_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    
    seller_id: Mapped[Optional[str]] = mapped_column(String(64), nullable=True, index=True)
    
    quantity: Mapped[float] = mapped_column(Float, nullable=False)
    unit: Mapped[str] = mapped_column(String(50), default="Quintal")
    price_per_unit: Mapped[float] = mapped_column(Float, nullable=False)
    total_price: Mapped[float] = mapped_column(Float, nullable=False)
    
    # States: PENDING, CONFIRMED, PROCESSING, READY, COMPLETED, CANCELLED, REJECTED
    status: Mapped[str] = mapped_column(String(50), default="CONFIRMED", index=True)
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    listing = relationship("WasteListing", back_populates="orders")

    def to_dict(self):
        return {
            "id": self.id,
            "order_number": self.order_number,
            "listing_id": self.listing_id,
            "listing_title": self.listing.title if self.listing else None,
            "buyer_id": self.buyer_id,
            "buyer_name": self.buyer_name,
            "buyer_phone": self.buyer_phone,
            "seller_id": self.seller_id,
            "quantity": self.quantity,
            "unit": self.unit,
            "price_per_unit": self.price_per_unit,
            "total_price": self.total_price,
            "status": self.status,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }
