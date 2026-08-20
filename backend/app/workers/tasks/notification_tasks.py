import requests
from loguru import logger
from app.workers.celery_app import celery_app
from app.core.config import settings

@celery_app.task(name="app.workers.tasks.notification_tasks.send_push_notification")
def send_push_notification(user_id: str, title: str, body: str, data: dict = None):
    logger.info(f"[PUSH] To user={user_id} | {title}: {body}")

@celery_app.task(name="app.workers.tasks.notification_tasks.send_sms")
def send_sms(phone: str, message: str):
    logger.info(f"[SMS] Attempting delivery to {phone}: {message}")
    
    if settings.SMS_PROVIDER == "fast2sms" and settings.FAST2SMS_API_KEY:
        try:
            clean_phone = phone.replace("+91", "").replace("+", "").strip()
            url = "https://www.fast2sms.com/dev/bulkV2"
            headers = {"authorization": settings.FAST2SMS_API_KEY}
            payload = {
                "variables_values": message,
                "route": "otp",
                "numbers": clean_phone
            }
            res = requests.post(url, data=payload, headers=headers, timeout=5)
            logger.info(f"[SMS Fast2SMS] Response: {res.text}")
            return res.status_code == 200
        except Exception as err:
            logger.error(f"[SMS Fast2SMS Error] Failed to send SMS: {err}")
    
    logger.info(f"[SMS Mock/Fallback] OTP Message logged: {message}")
    return True
