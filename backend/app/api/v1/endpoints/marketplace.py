from typing import Optional, List
from fastapi import APIRouter, Query, status
from pydantic import BaseModel

router = APIRouter(prefix="/waste", tags=["Waste Marketplace"])

class WasteListing(BaseModel):
    id: int
    title: str
    category: str
    quantity: str
    price: str
    location: str
    farmer_name: str
    image_url: str
    status: str = "Available"

DEMO_LISTINGS = [
    {
        "id": 1,
        "title": "Paddy Straw / Rice Husk (10 Tons)",
        "category": "paddy",
        "quantity": "10 Tons",
        "price": "₹1,800 / Ton",
        "location": "Ludhiana, Punjab",
        "farmer_name": "Gurpreet Singh",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    },
    {
        "id": 2,
        "title": "Sugarcane Bagasse & Tops",
        "category": "sugarcane",
        "quantity": "25 Tons",
        "price": "₹2,200 / Ton",
        "location": "Kolhapur, Maharashtra",
        "farmer_name": "Ramesh Patil",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    },
    {
        "id": 3,
        "title": "Wheat Straw Bales (Organic)",
        "category": "wheat",
        "quantity": "15 Tons",
        "price": "₹2,000 / Ton",
        "location": "Karnal, Haryana",
        "farmer_name": "Jaideep Malik",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    },
    {
        "id": 4,
        "title": "Cotton Stalk Waste for Bio-pellets",
        "category": "cotton",
        "quantity": "8 Tons",
        "price": "₹1,500 / Ton",
        "location": "Rajkot, Gujarat",
        "farmer_name": "Bhavik Patel",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    }
]

@router.get("", summary="Get agricultural waste listings")
async def get_waste_listings(category: Optional[str] = Query(None)):
    if category and category != "all":
        filtered = [item for item in DEMO_LISTINGS if item["category"].lower() == category.lower()]
        return {"status": "success", "data": filtered}
    return {"status": "success", "data": DEMO_LISTINGS}

@router.post("", status_code=status.HTTP_201_CREATED, summary="Post new waste listing")
async def create_waste_listing(listing: WasteListing):
    new_item = listing.model_dump()
    new_item["id"] = len(DEMO_LISTINGS) + 1
    DEMO_LISTINGS.insert(0, new_item)
    return {"status": "success", "message": "Waste listing created successfully", "data": new_item}
