"""
AgriLink AI — Real-time Chat & Escrow Messaging Endpoints
"""
from typing import List
from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter(prefix="/chat", tags=["Chat & Escrow"])

class ChatMessage(BaseModel):
    sender: str
    message: str

CHAT_HISTORY = [
    {
        "id": 1,
        "sender": "Buyer (GreenBio Energy Ltd)",
        "message": "Hi, we want to purchase your 10 Tons Paddy Straw. Is ₹1,800/Ton finalized?",
        "timestamp": "10:14 AM"
    },
    {
        "id": 2,
        "sender": "Farmer (Gurpreet)",
        "message": "Yes, rate is ₹1,800 per Ton. Total deal value ₹56,350 including logistics.",
        "timestamp": "10:16 AM"
    }
]

@router.get("/messages", summary="Get chat messages for current deal")
async def get_messages():
    return {"status": "success", "data": CHAT_HISTORY}

@router.post("/send", status_code=status.HTTP_201_CREATED, summary="Send message in chat channel")
async def send_message(msg: ChatMessage):
    new_msg = {
        "id": len(CHAT_HISTORY) + 1,
        "sender": msg.sender,
        "message": msg.message,
        "timestamp": "Just now"
    }
    CHAT_HISTORY.append(new_msg)
    
    reply = {
        "id": len(CHAT_HISTORY) + 1,
        "sender": "GreenBio Energy Ltd",
        "message": "Sounds great! Let's lock this price in Escrow so our transport truck can pick it up tomorrow.",
        "timestamp": "Just now"
    }
    CHAT_HISTORY.append(reply)
    
    return {"status": "success", "data": [new_msg, reply]}
