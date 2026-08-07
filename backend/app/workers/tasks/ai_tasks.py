"""
AgriLink AI — AI Inference Tasks (stub)
"""
from loguru import logger
from app.workers.celery_app import celery_app


@celery_app.task(name="app.workers.tasks.ai_tasks.run_disease_detection")
def run_disease_detection(image_url: str, user_id: str):
    logger.info(f"[AI] Disease detection for user={user_id} image={image_url}")
    # TODO: integrate TF/PyTorch model or external API
    return {"disease": "healthy", "confidence": 0.99}
