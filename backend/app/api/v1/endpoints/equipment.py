from fastapi import APIRouter, status, Query

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
        "source_type": "farmer",
        "rate": "₹800 / Hr",
        "owner": "Sukhdev Farmer Co.",
        "location": "Ambala, Haryana",
        "rating": 4.9,
        "image_url": "assets/agri_waste_banner.png",
        "available": True
    },
    {
        "id": 2,
        "name": "Kubota Combined Paddy Harvester",
        "category": "Harvesters",
        "source_type": "farmer",
        "rate": "₹2,500 / Hr",
        "owner": "Punjab Agri Rentals (Farmer)",
        "location": "Patiala, Punjab",
        "rating": 4.8,
        "image_url": "assets/agri_waste_banner.png",
        "available": True
    },
    {
        "id": 3,
        "name": "Heavy Duty Industrial Biomass Baler",
        "category": "Industrial Heavy",
        "source_type": "industry",
        "rate": "₹3,500 / Day",
        "owner": "IndoBio Energy Fleet (Industry)",
        "location": "Pithampur, MP",
        "rating": 4.9,
        "image_url": "assets/agri_waste_banner.png",
        "available": True
    }
]

@router.get("", summary="Get available machinery and equipment")
async def get_equipment_list(source_type: Optional[str] = Query(None)):
    results = DEMO_EQUIPMENT
    if source_type and source_type.lower() != "all":
        results = [item for item in results if item.get("source_type", "farmer").lower() == source_type.lower()]
    return {"status": "success", "data": results}

@router.post("/book", status_code=status.HTTP_200_OK, summary="Book equipment rental")
async def book_equipment(booking: EquipmentBooking):
    return {
        "status": "success",
        "message": f"Rental request submitted for Equipment #{booking.equipment_id} for {booking.days} days starting {booking.start_date}.",
        "booking_id": "EQ-89421"
    }
