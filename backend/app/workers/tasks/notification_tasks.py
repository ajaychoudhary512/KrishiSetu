from loguru import logger
from app.workers.celery_app import celery_app

@celery_app.task(name="app.workers.tasks.notification_tasks.send_push_notification")
def send_push_notification(user_id: str, title: str, body: str, data: dict = None):
    logger.info(f"[PUSH] To user={user_id} | {title}: {body}")

@celery_app.task(name="app.workers.tasks.notification_tasks.send_sms")
def send_sms(phone: str, message: str):
    logger.info(f"[SMS] To {phone}: {message}")
