from typing import Optional
from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter(prefix="/equipment", tags=["Equipment Rental"])

class EquipmentBooking(BaseModel):
    equipment_id: int
    days: int
    start_date: str

DEMO_EQUIPMENT = [
    {
        "id": 1,
        "name": "John Deere 5050D Tractor (50 HP)",
        "category": "Tractors",
        "rate": "₹800 / Hr",
        "owner": "Sukhdev Farmer Producer Co.",
        "location": "Ambala, Haryana",
        "rating": 4.9,
        "image_url": "assets/agri_waste_banner.png",
        "available": True
    },
    {
        "id": 2,
        "name": "Kubota Combined Paddy Harvester",
        "category": "Harvesters",
        "rate": "₹2,500 / Hr",
        "owner": "Punjab Agri Rentals",
        "location": "Patiala, Punjab",
        "rating": 4.8,
        "image_url": "assets/agri_waste_banner.png",
        "available": True
    },
    {
        "id": 3,
        "name": "Automatic Rotavator & Seed Drill",
        "category": "Seeders",
        "rate": "₹600 / Hr",
        "owner": "Kisan Seva Kendra",
        "location": "Hisar, Haryana",
        "rating": 4.7,
        "image_url": "assets/agri_waste_banner.png",
        "available": True
    }
]

@router.get("", summary="Get available machinery and equipment")
async def get_equipment_list():
    return {"status": "success", "data": DEMO_EQUIPMENT}

@router.post("/book", status_code=status.HTTP_200_OK, summary="Book equipment rental")
async def book_equipment(booking: EquipmentBooking):
    return {
        "status": "success",
        "message": f"Rental request submitted for Equipment #{booking.equipment_id} for {booking.days} days starting {booking.start_date}.",
        "booking_id": "EQ-89421"
    }
