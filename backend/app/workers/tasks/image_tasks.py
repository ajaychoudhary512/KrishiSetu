"""
AgriLink AI — Image Processing Tasks (stub)
"""
from loguru import logger
from app.workers.celery_app import celery_app


@celery_app.task(name="app.workers.tasks.image_tasks.generate_thumbnail")
def generate_thumbnail(image_url: str, size: tuple = (300, 300)):
    logger.info(f"[IMG] Generating {size} thumbnail for {image_url}")
    return {"thumbnail_url": image_url}
