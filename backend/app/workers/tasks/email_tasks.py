from loguru import logger

from app.workers.celery_app import celery_app

@celery_app.task(bind=True, name="app.workers.tasks.email_tasks.send_verification_email", max_retries=3)
def send_verification_email(self, email: str, full_name: str, token: str):
    
    try:
        verification_url = f"http://localhost:8000/api/v1/auth/verify-email?token={token}"
        logger.info(f"[EMAIL] Sending verification email to {email}: {verification_url}")
    except Exception as exc:
        logger.error(f"[EMAIL] Failed to send to {email}: {exc}")
        raise self.retry(exc=exc, countdown=60)

@celery_app.task(bind=True, name="app.workers.tasks.email_tasks.send_password_reset_email", max_retries=3)
def send_password_reset_email(self, email: str, full_name: str, token: str):
    
    try:
        reset_url = f"http://localhost:8000/api/v1/auth/reset-password?token={token}"
        logger.info(f"[EMAIL] Sending password-reset email to {email}: {reset_url}")
    except Exception as exc:
        raise self.retry(exc=exc, countdown=60)

@celery_app.task(bind=True, name="app.workers.tasks.email_tasks.send_welcome_email", max_retries=3)
def send_welcome_email(self, email: str, full_name: str):
    
    try:
        logger.info(f"[EMAIL] Sending welcome email to {full_name} <{email}>")
    except Exception as exc:
        raise self.retry(exc=exc, countdown=60)
