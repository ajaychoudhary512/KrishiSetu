"""
AgriLink AI — Celery Email & Notification Tasks (stubs)

These tasks are dispatched asynchronously by service-layer code.
In development they log instead of sending real emails/SMS.
"""
from loguru import logger

from app.workers.celery_app import celery_app


@celery_app.task(bind=True, name="app.workers.tasks.email_tasks.send_verification_email", max_retries=3)
def send_verification_email(self, email: str, full_name: str, token: str):
    """Send an email-verification link to the user."""
    try:
        verification_url = f"http://localhost:8000/api/v1/auth/verify-email?token={token}"
        logger.info(f"[EMAIL] Sending verification email to {email}: {verification_url}")
        # TODO: integrate fastapi-mail / SendGrid / SES here
    except Exception as exc:
        logger.error(f"[EMAIL] Failed to send to {email}: {exc}")
        raise self.retry(exc=exc, countdown=60)


@celery_app.task(bind=True, name="app.workers.tasks.email_tasks.send_password_reset_email", max_retries=3)
def send_password_reset_email(self, email: str, full_name: str, token: str):
    """Send a password-reset link to the user."""
    try:
        reset_url = f"http://localhost:8000/api/v1/auth/reset-password?token={token}"
        logger.info(f"[EMAIL] Sending password-reset email to {email}: {reset_url}")
    except Exception as exc:
        raise self.retry(exc=exc, countdown=60)


@celery_app.task(bind=True, name="app.workers.tasks.email_tasks.send_welcome_email", max_retries=3)
def send_welcome_email(self, email: str, full_name: str):
    """Send a welcome email after successful registration."""
    try:
        logger.info(f"[EMAIL] Sending welcome email to {full_name} <{email}>")
    except Exception as exc:
        raise self.retry(exc=exc, countdown=60)
