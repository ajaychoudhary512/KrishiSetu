"""
AgriLink AI — Main API v1 Router

Mounts all feature routers under /api/v1.
Add new feature routers here as the platform grows.
"""
from fastapi import APIRouter

from app.api.v1.endpoints.auth import router as auth_router
from app.api.v1.endpoints.users import router as users_router
from app.api.v1.endpoints.marketplace import router as marketplace_router
from app.api.v1.endpoints.equipment import router as equipment_router
from app.api.v1.endpoints.labor import router as labor_router
from app.api.v1.endpoints.disease_detection import router as disease_router
from app.api.v1.endpoints.chat import router as chat_router
from app.api.v1.endpoints.wallet import router as wallet_router

api_router = APIRouter()

# ── Core authentication & user management ─────────────────────────────────────
api_router.include_router(auth_router)
api_router.include_router(users_router)
api_router.include_router(marketplace_router)
api_router.include_router(equipment_router)
api_router.include_router(labor_router)
api_router.include_router(disease_router)
api_router.include_router(chat_router)
api_router.include_router(wallet_router)

