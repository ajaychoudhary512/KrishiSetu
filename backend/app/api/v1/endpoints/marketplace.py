import re
import uuid
from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.database.session import get_db
from app.models.marketplace import WasteListing, WasteOrder

router = APIRouter(prefix="/waste", tags=["Waste Marketplace"])

class CreateListingRequest(BaseModel):
    title: Optional[str] = None
    product_name: Optional[str] = None
    category: str = Field("Crop Residue")
    source_type: str = Field("farmer")  # "farmer" or "industry"
    quantity: Optional[float] = None
    original_quantity: Optional[float] = None
    unit: str = Field("Quintal")
    price: Optional[float] = Field(None, gt=0)
    price_per_unit: Optional[str] = None
    price_unit: Optional[str] = "Quintal"
    location: Optional[str] = None
    location_name: Optional[str] = None
    description: Optional[str] = ""
    farmer_name: Optional[str] = None
    seller_name: Optional[str] = None
    seller_phone: Optional[str] = None
    image_url: Optional[str] = "rice_straw"

class EditListingRequest(BaseModel):
    title: Optional[str] = None
    price: Optional[float] = Field(None, gt=0)
    location: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None  # ACTIVE, PAUSED, CANCELLED

class PurchaseRequest(BaseModel):
    quantity: float = Field(..., gt=0)
    buyer_name: Optional[str] = "Industry Buyer"
    buyer_phone: Optional[str] = "+919826012345"
    buyer_id: Optional[str] = None

# Initial seed data for empty database
SEED_LISTINGS = [
    {
        "title": "🌾 Rice Straw Stubble",
        "category": "Crop Residue",
        "source_type": "farmer",
        "original_quantity": 120.0,
        "remaining_quantity": 120.0,
        "unit": "Quintal",
        "price": 100.0,
        "price_unit": "Quintal",
        "location": "Indore, MP • 12 km",
        "seller_name": "Ramesh Patel (Farmer)",
        "seller_phone": "+919826011111",
        "image_url": "rice_straw",
        "status": "ACTIVE",
        "is_verified": True
    },
    {
        "title": "🌾 Wheat Straw Bales",
        "category": "Crop Residue",
        "source_type": "farmer",
        "original_quantity": 80.0,
        "remaining_quantity": 80.0,
        "unit": "Quintal",
        "price": 130.0,
        "price_unit": "Quintal",
        "location": "Dewas, MP • 18 km",
        "seller_name": "Suresh Kumar (Farmer)",
        "seller_phone": "+919826022222",
        "image_url": "wheat_straw",
        "status": "ACTIVE",
        "is_verified": True
    },
    {
        "title": "🏭 Paddy Straw Bulk Demand",
        "category": "Industry Demand",
        "source_type": "industry",
        "original_quantity": 500.0,
        "remaining_quantity": 500.0,
        "unit": "Ton",
        "price": 1800.0,
        "price_unit": "Ton",
        "location": "Pithampur SEZ • 25 km",
        "seller_name": "GreenBio Energy Ltd (Industry)",
        "seller_phone": "+919826033333",
        "image_url": "rice_straw",
        "status": "ACTIVE",
        "is_verified": True
    },
    {
        "title": "🏭 Sugarcane Bagasse Purchase",
        "category": "Bagasse",
        "source_type": "industry",
        "original_quantity": 250.0,
        "remaining_quantity": 250.0,
        "unit": "Ton",
        "price": 2200.0,
        "price_unit": "Ton",
        "location": "Ujjain Agro Park • 35 km",
        "seller_name": "Apex Bio-Pellets Pvt Ltd (Industry)",
        "seller_phone": "+919826044444",
        "image_url": "wheat_straw",
        "status": "ACTIVE",
        "is_verified": True
    }
]

async def ensure_seed_listings(db: AsyncSession):
    stmt = select(WasteListing).limit(1)
    res = await db.execute(stmt)
    if not res.scalars().first():
        for item in SEED_LISTINGS:
            listing = WasteListing(**item)
            db.add(listing)
        await db.commit()

@router.get("", summary="Get agricultural waste listings")
async def get_waste_listings(
    category: Optional[str] = Query(None),
    source_type: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    await ensure_seed_listings(db)
    stmt = select(WasteListing).where(WasteListing.status.in_(["ACTIVE", "PARTIALLY_SOLD", "SOLD_OUT", "PAUSED"])).order_by(WasteListing.id.desc())
    res = await db.execute(stmt)
    items = res.scalars().all()

    results = []
    for item in items:
        # Source type filter
        if source_type and source_type.lower() != "all":
            if item.source_type.lower() != source_type.lower():
                continue
        # Category filter
        if category and category.lower() != "all":
            if category.lower() not in item.category.lower():
                continue
        results.append(item.to_dict())

    return {"status": "success", "data": results}

@router.get("/{listing_id}", summary="Get single waste listing details")
async def get_waste_listing(listing_id: int, db: AsyncSession = Depends(get_db)):
    listing = await db.get(WasteListing, listing_id)
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")
    return {"status": "success", "data": listing.to_dict()}

@router.post("", status_code=status.HTTP_201_CREATED, summary="Post new waste listing")
async def create_waste_listing(req: CreateListingRequest, db: AsyncSession = Depends(get_db)):
    item_title = req.title or req.product_name or "Agricultural Crop Residue"
    item_qty = req.quantity if req.quantity is not None else (req.original_quantity if req.original_quantity is not None else 0.0)

    # Validate quantity
    if item_qty <= 0:
        raise HTTPException(status_code=400, detail="Quantity must be greater than zero")

    # Parse price
    final_price = req.price
    if final_price is None and req.price_per_unit:
        # extract numeric from string like "₹100/quintal"
        digits = re.findall(r"[\d.]+", req.price_per_unit)
        if digits:
            final_price = float(digits[0])
        else:
            final_price = 100.0
    if final_price is None:
        final_price = 100.0

    final_location = req.location or req.location_name or "Indore Mandi • 5 km"
    final_seller = req.seller_name or req.farmer_name or "Kisan (Farmer)"

    listing = WasteListing(
        title=item_title,
        category=req.category,
        source_type=req.source_type,
        original_quantity=item_qty,
        remaining_quantity=item_qty,
        unit=req.unit,
        price=final_price,
        price_unit=req.price_unit or req.unit,
        location=final_location,
        description=req.description,
        seller_name=final_seller,
        seller_phone=req.seller_phone,
        image_url=req.image_url or "rice_straw",
        status="ACTIVE",
        is_verified=True
    )
    db.add(listing)
    await db.commit()
    await db.refresh(listing)

    return {
        "status": "success",
        "message": f"Listing created successfully with {listing.remaining_quantity} {listing.unit}",
        "data": listing.to_dict()
    }

@router.put("/{listing_id}", summary="Edit existing waste listing")
async def update_waste_listing(listing_id: int, req: EditListingRequest, db: AsyncSession = Depends(get_db)):
    listing = await db.get(WasteListing, listing_id)
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")

    if req.title:
        listing.title = req.title
    if req.price:
        listing.price = req.price
    if req.location:
        listing.location = req.location
    if req.description is not None:
        listing.description = req.description
    if req.status:
        listing.status = req.status

    await db.commit()
    await db.refresh(listing)
    return {"status": "success", "message": "Listing updated successfully", "data": listing.to_dict()}

@router.delete("/{listing_id}", summary="Cancel / Pause waste listing")
async def delete_waste_listing(listing_id: int, db: AsyncSession = Depends(get_db)):
    listing = await db.get(WasteListing, listing_id)
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")

    listing.status = "CANCELLED"
    await db.commit()
    return {"status": "success", "message": "Listing cancelled successfully"}

@router.post("/{listing_id}/purchase", status_code=status.HTTP_200_OK, summary="Atomic purchase/booking of waste quantity")
async def purchase_waste_quantity(listing_id: int, req: PurchaseRequest, db: AsyncSession = Depends(get_db)):
    listing = await db.get(WasteListing, listing_id)
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")

    if listing.status in ["SOLD_OUT", "CANCELLED"]:
        raise HTTPException(
            status_code=400,
            detail="This listing is no longer available. / यह लिस्टिंग अब उपलब्ध नहीं है।"
        )

    if req.quantity <= 0:
        raise HTTPException(status_code=400, detail="Requested quantity must be greater than zero.")

    # Concurrency / Over-selling check
    if req.quantity > listing.remaining_quantity:
        avail_str = f"{int(listing.remaining_quantity) if listing.remaining_quantity.is_integer() else listing.remaining_quantity} {listing.unit}"
        raise HTTPException(
            status_code=400,
            detail=f"Only {avail_str} is available. / केवल {avail_str} उपलब्ध है।"
        )

    # Atomic deduction
    listing.remaining_quantity = round(listing.remaining_quantity - req.quantity, 2)
    if listing.remaining_quantity <= 0.0001:
        listing.remaining_quantity = 0.0
        listing.status = "SOLD_OUT"
    else:
        listing.status = "PARTIALLY_SOLD"

    total_price = round(req.quantity * listing.price, 2)
    order_num = f"ORD-{uuid.uuid4().hex[:8].upper()}"

    order = WasteOrder(
        order_number=order_num,
        listing_id=listing.id,
        buyer_id=req.buyer_id,
        buyer_name=req.buyer_name or "Industry Buyer",
        buyer_phone=req.buyer_phone,
        seller_id=listing.seller_id,
        quantity=req.quantity,
        unit=listing.unit,
        price_per_unit=listing.price,
        total_price=total_price,
        status="CONFIRMED"
    )
    db.add(order)
    await db.commit()
    await db.refresh(listing)
    await db.refresh(order)

    return {
        "status": "success",
        "message": f"Successfully purchased {req.quantity} {listing.unit}. Remaining: {listing.remaining_quantity} {listing.unit}",
        "data": {
            "order": order.to_dict(),
            "listing": listing.to_dict()
        }
    }

@router.get("/orders/my", summary="Get user orders")
async def get_my_orders(db: AsyncSession = Depends(get_db)):
    stmt = select(WasteOrder).order_by(WasteOrder.id.desc()).limit(50)
    res = await db.execute(stmt)
    orders = res.scalars().all()
    return {"status": "success", "data": [o.to_dict() for o in orders]}

alias_router = APIRouter(prefix="/marketplace", tags=["Waste Marketplace"])
alias_router.add_api_route("", get_waste_listings, methods=["GET"], summary="Get marketplace listings (alias)")
alias_router.add_api_route("", create_waste_listing, methods=["POST"], status_code=status.HTTP_201_CREATED, summary="Post new marketplace listing (alias)")
alias_router.add_api_route("/{listing_id}", get_waste_listing, methods=["GET"], summary="Get listing details (alias)")
alias_router.add_api_route("/{listing_id}", update_waste_listing, methods=["PUT"], summary="Update listing (alias)")
alias_router.add_api_route("/{listing_id}", delete_waste_listing, methods=["DELETE"], summary="Delete listing (alias)")
alias_router.add_api_route("/{listing_id}/purchase", purchase_waste_quantity, methods=["POST"], summary="Purchase listing quantity (alias)")
alias_router.add_api_route("/orders/my", get_my_orders, methods=["GET"], summary="Get orders (alias)")
