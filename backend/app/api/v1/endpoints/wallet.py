"""
AgriLink AI — Escrow & Wallet Endpoints
"""
from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter(prefix="/wallet", tags=["Wallet & Escrow"])

class EscrowDeposit(BaseModel):
    amount: float
    deal_id: str

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
