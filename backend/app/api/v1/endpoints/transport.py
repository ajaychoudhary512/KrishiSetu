from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database.session import get_db
from app.models.transport import TransportVehicle, TransportRequest

router = APIRouter(prefix="/transport", tags=["Transport Services"])

class AddVehicleRequest(BaseModel):
    transporter_name: str = Field(..., min_length=2)
    phone: Optional[str] = None
    vehicle_type: str = Field("Truck (10 Wheeler)")
    capacity: str = Field("15 Tons")
    rate_per_km: float = Field(45.0, gt=0)
    location: str = Field("Indore, MP")
    available: bool = True

class BookTransportRequest(BaseModel):
    vehicle_id: Optional[int] = None
    customer_name: Optional[str] = "Farmer"
    customer_phone: Optional[str] = "+919826012345"
    pickup_location: str
    drop_location: str
    crop_type: str = "Rice Straw Stubble"
    quantity: str = "10 Tons"
    estimated_cost: float = 3500.0

SEED_VEHICLES = [
    {
        "transporter_name": "Kisan Express Logistics",
        "phone": "+919826010001",
        "vehicle_type": "Truck (12 Wheeler)",
        "capacity": "20 Tons Stubble / Biomass",
        "rate_per_km": 42.0,
        "location": "Indore - Dewas Bypass",
        "available": True,
        "status": "ACTIVE"
    },
    {
        "transporter_name": "Malwa Agro Cargo Fleet",
        "phone": "+919826010002",
        "vehicle_type": "Tractor Trolley High-Side",
        "capacity": "8 Tons Straw Bales",
        "rate_per_km": 28.0,
        "location": "Ujjain - Pithampur Corridor",
        "available": True,
        "status": "ACTIVE"
    }
]

async def ensure_seed_transport(db: AsyncSession):
    stmt = select(TransportVehicle).limit(1)
    res = await db.execute(stmt)
    if not res.scalars().first():
        for v in SEED_VEHICLES:
            db.add(TransportVehicle(**v))
        await db.commit()

@router.get("", summary="Get available transport vehicles")
@router.get("/vehicles", summary="Get available transport vehicles (alias)")
async def get_transport_vehicles(db: AsyncSession = Depends(get_db)):
    await ensure_seed_transport(db)
    stmt = select(TransportVehicle).where(TransportVehicle.status == "ACTIVE").order_by(TransportVehicle.id.desc())
    res = await db.execute(stmt)
    vehicles = res.scalars().all()
    return {"status": "success", "data": [v.to_dict() for v in vehicles]}

@router.post("/vehicle", status_code=status.HTTP_201_CREATED, summary="Add a new transport vehicle")
async def add_vehicle(req: AddVehicleRequest, db: AsyncSession = Depends(get_db)):
    veh = TransportVehicle(
        transporter_name=req.transporter_name,
        phone=req.phone,
        vehicle_type=req.vehicle_type,
        capacity=req.capacity,
        rate_per_km=req.rate_per_km,
        location=req.location,
        available=req.available,
        status="ACTIVE"
    )
    db.add(veh)
    await db.commit()
    await db.refresh(veh)
    return {"status": "success", "message": "Transport vehicle added successfully", "data": veh.to_dict()}

@router.post("/request", status_code=status.HTTP_201_CREATED, summary="Submit a transport delivery request")
async def request_transport(req: BookTransportRequest, db: AsyncSession = Depends(get_db)):
    tr = TransportRequest(
        vehicle_id=req.vehicle_id,
        customer_name=req.customer_name or "Farmer",
        customer_phone=req.customer_phone,
        pickup_location=req.pickup_location,
        drop_location=req.drop_location,
        crop_type=req.crop_type,
        quantity=req.quantity,
        estimated_cost=req.estimated_cost,
        status="REQUESTED"
    )
    db.add(tr)
    await db.commit()
    await db.refresh(tr)
    return {"status": "success", "message": "Transport request dispatched", "data": tr.to_dict()}

@router.get("/requests/my", summary="Get user transport requests")
async def get_my_transport_requests(db: AsyncSession = Depends(get_db)):
    stmt = select(TransportRequest).order_by(TransportRequest.id.desc()).limit(50)
    res = await db.execute(stmt)
    reqs = res.scalars().all()
    return {"status": "success", "data": [r.to_dict() for r in reqs]}
