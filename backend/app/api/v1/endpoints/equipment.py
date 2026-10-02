from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database.session import get_db
from app.models.equipment import EquipmentListing, EquipmentBooking

router = APIRouter(prefix="/equipment", tags=["Equipment Rental"])

class CreateEquipmentRequest(BaseModel):
    name: str = Field(..., min_length=2)
    category: str = Field("Tractor")
    source_type: str = Field("farmer")
    rate: float = Field(..., gt=0)
    rate_unit: str = Field("Hr")
    location: str = Field(...)
    owner_name: Optional[str] = "Equipment Owner"
    owner_phone: Optional[str] = None
    image_url: Optional[str] = "assets/agri_waste_banner.png"

class EquipmentBookingRequest(BaseModel):
    equipment_id: int
    days: int = Field(1, ge=1)
    start_date: str
    renter_name: Optional[str] = "Farmer"
    renter_phone: Optional[str] = "+919826012345"

SEED_EQUIPMENT = [
    {
        "name": "John Deere 5050D Tractor (50 HP)",
        "category": "Tractors",
        "source_type": "farmer",
        "rate": 800.0,
        "rate_unit": "Hr",
        "owner_name": "Sukhdev Farmer Co.",
        "owner_phone": "+919826055555",
        "location": "Ambala, Haryana",
        "rating": 4.9,
        "image_url": "assets/agri_waste_banner.png",
        "available": True,
        "status": "ACTIVE"
    },
    {
        "name": "Kubota Combined Paddy Harvester",
        "category": "Harvesters",
        "source_type": "farmer",
        "rate": 2500.0,
        "rate_unit": "Hr",
        "owner_name": "Punjab Agri Rentals (Farmer)",
        "owner_phone": "+919826066666",
        "location": "Patiala, Punjab",
        "rating": 4.8,
        "image_url": "assets/agri_waste_banner.png",
        "available": True,
        "status": "ACTIVE"
    },
    {
        "name": "Heavy Duty Industrial Biomass Baler",
        "category": "Industrial Heavy",
        "source_type": "industry",
        "rate": 3500.0,
        "rate_unit": "Day",
        "owner_name": "IndoBio Energy Fleet (Industry)",
        "owner_phone": "+919826077777",
        "location": "Pithampur, MP",
        "rating": 4.9,
        "image_url": "assets/agri_waste_banner.png",
        "available": True,
        "status": "ACTIVE"
    }
]

async def ensure_seed_equipment(db: AsyncSession):
    stmt = select(EquipmentListing).limit(1)
    res = await db.execute(stmt)
    if not res.scalars().first():
        for item in SEED_EQUIPMENT:
            db.add(EquipmentListing(**item))
        await db.commit()

@router.get("", summary="Get available machinery and equipment")
async def get_equipment_list(
    source_type: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    await ensure_seed_equipment(db)
    stmt = select(EquipmentListing).where(EquipmentListing.status == "ACTIVE").order_by(EquipmentListing.id.desc())
    res = await db.execute(stmt)
    items = res.scalars().all()

    results = []
    for item in items:
        if source_type and source_type.lower() != "all":
            if item.source_type.lower() != source_type.lower():
                continue
        results.append(item.to_dict())
    return {"status": "success", "data": results}

@router.post("", status_code=status.HTTP_201_CREATED, summary="Add new equipment listing")
async def create_equipment(req: CreateEquipmentRequest, db: AsyncSession = Depends(get_db)):
    eq = EquipmentListing(
        name=req.name,
        category=req.category,
        source_type=req.source_type,
        rate=req.rate,
        rate_unit=req.rate_unit,
        location=req.location,
        owner_name=req.owner_name or "Equipment Owner",
        owner_phone=req.owner_phone,
        image_url=req.image_url or "assets/agri_waste_banner.png",
        available=True,
        status="ACTIVE"
    )
    db.add(eq)
    await db.commit()
    await db.refresh(eq)
    return {"status": "success", "message": "Equipment listing added successfully", "data": eq.to_dict()}

@router.post("/book", status_code=status.HTTP_200_OK, summary="Book equipment rental")
async def book_equipment(req: EquipmentBookingRequest, db: AsyncSession = Depends(get_db)):
    eq = await db.get(EquipmentListing, req.equipment_id)
    if not eq:
        raise HTTPException(status_code=404, detail="Equipment not found")

    total_cost = round(eq.rate * req.days, 2)
    booking = EquipmentBooking(
        equipment_id=eq.id,
        renter_name=req.renter_name or "Farmer",
        renter_phone=req.renter_phone,
        days=req.days,
        start_date=req.start_date,
        total_cost=total_cost,
        status="REQUESTED"
    )
    db.add(booking)
    await db.commit()
    await db.refresh(booking)

    return {
        "status": "success",
        "message": f"Rental request submitted for {eq.name} for {req.days} days starting {req.start_date}.",
        "booking": booking.to_dict()
    }

@router.get("/bookings/my", summary="Get user equipment bookings")
async def get_my_bookings(db: AsyncSession = Depends(get_db)):
    stmt = select(EquipmentBooking).order_by(EquipmentBooking.id.desc()).limit(50)
    res = await db.execute(stmt)
    bookings = res.scalars().all()
    return {"status": "success", "data": [b.to_dict() for b in bookings]}
