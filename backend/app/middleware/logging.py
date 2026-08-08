import time

from loguru import logger
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response

class LoggingMiddleware(BaseHTTPMiddleware):
    

    async def dispatch(self, request: Request, call_next) -> Response:
        start_time = time.perf_counter()
        request_id = getattr(request.state, "request_id", "unknown")

        logger.info(
            f"→ {request.method} {request.url.path} | "
            f"client={request.client.host if request.client else 'unknown'} | "
            f"id={request_id}"
        )

        try:
            response = await call_next(request)
        except Exception as exc:
            duration = (time.perf_counter() - start_time) * 1000
            logger.error(
                f"✗ {request.method} {request.url.path} | "
                f"error={exc!r} | {duration:.1f}ms | id={request_id}"
            )
            raise

        duration = (time.perf_counter() - start_time) * 1000
        logger.info(
            f"← {request.method} {request.url.path} | "
            f"status={response.status_code} | {duration:.1f}ms | id={request_id}"
        )
        return response
