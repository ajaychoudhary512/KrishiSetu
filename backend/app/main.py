"""
AgriLink AI — Application Entry Point
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
from fastapi.responses import JSONResponse

from app.core.config import settings
from app.core.exceptions import register_exception_handlers
from app.database.session import engine
from app.database.base import Base
from app.middleware.logging import LoggingMiddleware
from app.middleware.request_id import RequestIDMiddleware
from app.api.v1.router import api_router

from prometheus_fastapi_instrumentator import Instrumentator


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan: startup → yield → shutdown."""
    from loguru import logger
    try:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        logger.info("Database tables created/verified successfully.")
    except Exception as exc:
        logger.warning(
            f"Could not connect to database on startup: {exc}. "
            "Running without DB — endpoints requiring DB will return 503."
        )
    yield
    try:
        await engine.dispose()
    except Exception:
        pass


def create_application() -> FastAPI:
    """Factory function for the FastAPI application."""
    application = FastAPI(
        title=settings.APP_NAME,
        version=settings.APP_VERSION,
        description="""

Production-ready REST API for an AI-powered agriculture marketplace.

- 🔐 JWT Authentication with Refresh Tokens
- 🌾 Agricultural Waste Marketplace
- 🚜 Equipment Rental & Selling
- 👷 Labor Marketplace
- 🚛 Transport Marketplace
- 💬 Real-Time Chat (WebSockets)
- 🏛️ Government Schemes
- 🔬 AI Disease Detection
- 🤖 AI Chatbot
- 🔔 Notifications
- 💰 Wallet & Transactions
- ⭐ Ratings & Reviews
        """,
        openapi_url="/api/v1/openapi.json",
        docs_url="/docs",
        redoc_url="/redoc",
        lifespan=lifespan,
    )

    application.add_middleware(RequestIDMiddleware)
    application.add_middleware(LoggingMiddleware)
    application.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    application.add_middleware(
        TrustedHostMiddleware,
        allowed_hosts=settings.ALLOWED_HOSTS,
    )

    register_exception_handlers(application)

    application.include_router(api_router, prefix="/api/v1")

    Instrumentator().instrument(application).expose(application)

    @application.get("/health", tags=["Health"])
    async def health_check():
        return JSONResponse({"status": "ok", "version": settings.APP_VERSION})

    return application


app = create_application()
