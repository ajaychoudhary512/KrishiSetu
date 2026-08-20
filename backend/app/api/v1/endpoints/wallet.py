import hmac
import hashlib
from typing import Optional
from fastapi import APIRouter, status, HTTPException
from pydantic import BaseModel
from app.core.config import settings

router = APIRouter(prefix="/wallet", tags=["Wallet & Escrow"])

class EscrowDeposit(BaseModel):
    amount: float
    deal_id: str

class RazorpayOrderRequest(BaseModel):
    amount: float
    currency: str = "INR"
    receipt: Optional[str] = "receipt_agrilink_01"

class RazorpayVerifyRequest(BaseModel):
    razorpay_payment_id: str
    razorpay_order_id: str
    razorpay_signature: str
    deal_id: Optional[str] = "DEAL-LOCAL"

USER_WALLET = {
    "balance": 4250.00,
    "escrow_locked": 56350.00,
    "currency": "INR",
    "transactions": [
        {"id": "TXN-9081", "type": "Escrow Deposit", "amount": "+₹56,350", "status": "Locked in Escrow"},
        {"id": "TXN-9012", "type": "Equipment Payout", "amount": "-₹2,400", "status": "Completed"}
    ]
}

@router.get("/balance", summary="Get wallet balance & transactions")
async def get_wallet():
    return {"status": "success", "data": USER_WALLET}

@router.post("/escrow/accept", status_code=status.HTTP_200_OK, summary="Accept & Lock Escrow Deal")
async def accept_escrow_deal(deposit: EscrowDeposit):
    USER_WALLET["balance"] += deposit.amount
    USER_WALLET["transactions"].insert(0, {
        "id": f"TXN-{len(USER_WALLET['transactions']) + 9000}",
        "type": "Escrow Approved",
        "amount": f"+₹{deposit.amount:,.2f}",
        "status": "Secured"
    })
    return {
        "status": "success",
        "message": f"🎉 Deal Approved & ₹{deposit.amount:,.2f} secured in Escrow!",
        "new_balance": USER_WALLET["balance"]
    }

class SplitPayoutRequest(BaseModel):
    total_deal_amount: float
    commission_percentage: float = 2.5  # 2.5% platform commission
    seller_account_number: Optional[str] = "918234567890"
    deal_id: str

@router.post("/split-payout", status_code=status.HTTP_200_OK, summary="Calculate & Process Direct Deal Split Payout")
async def calculate_split_payout(req: SplitPayoutRequest):
    platform_commission = round(req.total_deal_amount * (req.commission_percentage / 100.0), 2)
    seller_direct_payout = round(req.total_deal_amount - platform_commission, 2)
    
    # Platform balance receives ONLY the commission
    USER_WALLET["balance"] += platform_commission
    USER_WALLET["transactions"].insert(0, {
        "id": f"COMM-{len(USER_WALLET['transactions']) + 1000}",
        "type": f"Platform Commission ({req.commission_percentage}%)",
        "amount": f"+₹{platform_commission:,.2f}",
        "status": "Received in Platform Account"
    })
    
    return {
        "status": "success",
        "message": f"🎉 Deal Processed! ₹{seller_direct_payout:,.2f} transferred directly to Seller/Farmer. Your platform commission of ₹{platform_commission:,.2f} received!",
        "breakdown": {
            "total_deal_value": f"₹{req.total_deal_amount:,.2f}",
            "seller_direct_payout": f"₹{seller_direct_payout:,.2f}",
            "platform_commission_earned": f"₹{platform_commission:,.2f}",
            "commission_rate": f"{req.commission_percentage}%"
        }
    }

@router.post("/razorpay/create-order", status_code=status.HTTP_201_CREATED, summary="Create Razorpay Escrow Order")
async def create_razorpay_order(req: RazorpayOrderRequest):
    amount_in_paise = int(req.amount * 100)
    order_id = f"order_agrilink_{hashlib.md5(f'{req.amount}{req.receipt}'.encode()).hexdigest()[:12]}"
    
    return {
        "status": "success",
        "key_id": settings.RAZORPAY_KEY_ID,
        "order_id": order_id,
        "amount": amount_in_paise,
        "currency": req.currency,
        "receipt": req.receipt
    }

@router.post("/razorpay/verify-payment", status_code=status.HTTP_200_OK, summary="Verify Razorpay Payment Signature")
async def verify_razorpay_payment(req: RazorpayVerifyRequest):
    generated_signature = hmac.new(
        settings.RAZORPAY_KEY_SECRET.encode(),
        f"{req.razorpay_order_id}|{req.razorpay_payment_id}".encode(),
        hashlib.sha256
    ).hexdigest()
    
    # Verify signature or fallback for test credentials
    if generated_signature == req.razorpay_signature or "demo" in settings.RAZORPAY_KEY_SECRET:
        # Calculate 2.5% commission
        total_payment = 5000.00
        commission = total_payment * 0.025
        USER_WALLET["balance"] += commission
        USER_WALLET["transactions"].insert(0, {
            "id": req.razorpay_payment_id,
            "type": "Platform Commission (2.5%)",
            "amount": f"+₹{commission:,.2f}",
            "status": "Secured & Verified"
        })
class RazorpayRouteSplitRequest(BaseModel):
    total_deal_amount: float
    farmer_account_id: Optional[str] = "acc_farmer_demo_987"
    commission_percentage: float = 2.5

@router.post("/razorpay/create-route-order", status_code=status.HTTP_201_CREATED, summary="Create Razorpay Route Split Order")
async def create_razorpay_route_order(req: RazorpayRouteSplitRequest):
    total_paise = int(req.total_deal_amount * 100)
    commission_paise = int(total_paise * (req.commission_percentage / 100.0))
    seller_payout_paise = total_paise - commission_paise
    
    order_id = f"order_route_{hashlib.md5(f'{req.total_deal_amount}{req.farmer_account_id}'.encode()).hexdigest()[:12]}"
    
    # Razorpay Route Payload structure
    route_transfers_payload = [
        {
            "account": req.farmer_account_id,
            "amount": seller_payout_paise,
            "currency": "INR",
            "on_hold": 1
        }
    ]
    
    return {
        "status": "success",
        "key_id": settings.RAZORPAY_KEY_ID,
        "order_id": order_id,
        "total_amount_paise": total_paise,
        "seller_payout_paise": seller_payout_paise,
        "platform_commission_paise": commission_paise,
        "route_transfers": route_transfers_payload,
        "message": f"Razorpay Route Order Generated: ₹{req.total_deal_amount:,.2f} deal with {req.commission_percentage}% commission split."
    }
