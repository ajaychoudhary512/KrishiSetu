"""
AgriLink AI — Celery Application

Configures Celery with Redis as the broker and result backend.
Workers handle email, SMS, AI inference, and image processing tasks.
"""
from celery import Celery

from app.core.config import settings

celery_app = Celery(
    "agrilink",
    broker=settings.CELERY_BROKER_URL,
    backend=settings.CELERY_RESULT_BACKEND,
    include=[
        "app.workers.tasks.email_tasks",
        "app.workers.tasks.notification_tasks",
        "app.workers.tasks.ai_tasks",
        "app.workers.tasks.image_tasks",
    ],
)

celery_app.conf.update(
    # Serialization
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="Asia/Kolkata",
    enable_utc=True,

    # Task routing
    task_routes={
        "app.workers.tasks.email_tasks.*": {"queue": "email"},
        "app.workers.tasks.notification_tasks.*": {"queue": "notifications"},
        "app.workers.tasks.ai_tasks.*": {"queue": "ai_inference"},
        "app.workers.tasks.image_tasks.*": {"queue": "image_processing"},
    },

    # Retry config
    task_acks_late=True,
    task_reject_on_worker_lost=True,
    task_max_retries=3,
    task_default_retry_delay=60,

    # Result expiry
    result_expires=3600,

    # Beat schedule (periodic tasks)
    beat_schedule={},
)
