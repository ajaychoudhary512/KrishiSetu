from typing import Optional, List
from fastapi import APIRouter, Query, status
from pydantic import BaseModel

router = APIRouter(prefix="/waste", tags=["Waste Marketplace"])

class WasteListing(BaseModel):
    id: Optional[int] = None
    title: str
    category: str
    source_type: str = "farmer"  # "farmer" or "industry"
    quantity: str
    price: str
    location: str
    farmer_name: str = "Agri User"
    image_url: str = "assets/agri_waste_banner.png"
    status: str = "Available"

DEMO_LISTINGS = [
    {
        "id": 1,
        "title": "Paddy Straw / Rice Husk (10 Tons)",
        "category": "paddy",
        "source_type": "farmer",
        "quantity": "10 Tons",
        "price": "₹1,800 / Ton",
        "location": "Ludhiana, Punjab",
        "farmer_name": "Gurpreet Singh (Farmer)",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    },
    {
        "id": 2,
        "title": "Sugarcane Bagasse & Tops",
        "category": "sugarcane",
        "source_type": "farmer",
        "quantity": "25 Tons",
        "price": "₹2,200 / Ton",
        "location": "Kolhapur, Maharashtra",
        "farmer_name": "Ramesh Patil (Farmer)",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    },
    {
        "id": 3,
        "title": "Bulk Biomass Paddy Straw Requirement",
        "category": "paddy",
        "source_type": "industry",
        "quantity": "100 Tons",
        "price": "₹1,950 / Ton (Target)",
        "location": "Pithampur, MP",
        "farmer_name": "GreenBio Energy Ltd (Industry)",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Buying Demand"
    },
    {
        "id": 4,
        "title": "Cotton Stalk Waste for Bio-pellets",
        "category": "cotton",
        "source_type": "farmer",
        "quantity": "8 Tons",
        "price": "₹1,500 / Ton",
        "location": "Rajkot, Gujarat",
        "farmer_name": "Bhavik Patel (Farmer)",
        "image_url": "assets/agri_waste_banner.png",
        "status": "Available"
    }
]

@router.get("", summary="Get agricultural waste listings")
async def get_waste_listings(
    category: Optional[str] = Query(None),
    source_type: Optional[str] = Query(None)
):
    results = DEMO_LISTINGS
    if source_type and source_type.lower() != "all":
        results = [item for item in results if item.get("source_type", "farmer").lower() == source_type.lower()]
    if category and category.lower() != "all":
        results = [item for item in results if item["category"].lower() == category.lower()]
    return {"status": "success", "data": results}

@router.post("", status_code=status.HTTP_201_CREATED, summary="Post new waste listing")
async def create_waste_listing(listing: WasteListing):
    new_item = listing.model_dump()
    new_item["id"] = len(DEMO_LISTINGS) + 1
    DEMO_LISTINGS.insert(0, new_item)
    return {"status": "success", "message": f"{new_item['source_type'].capitalize()} listing created successfully", "data": new_item}
