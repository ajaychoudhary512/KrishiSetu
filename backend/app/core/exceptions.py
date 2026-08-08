"""
AgriLink AI — Custom Exceptions & Global Exception Handlers

Defines a hierarchy of application-level exceptions and registers
FastAPI handlers that return consistent JSON error responses.
"""
from typing import Any, List, Optional

from fastapi import FastAPI, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from jose import JWTError
from pydantic import ValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException


class AppException(Exception):
    """Base application exception."""

    def __init__(
        self,
        message: str = "An unexpected error occurred",
        status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR,
        errors: Optional[List[Any]] = None,
    ):
        self.message = message
        self.status_code = status_code
        self.errors = errors or []
        super().__init__(message)


class AuthenticationError(AppException):
    def __init__(self, message: str = "Authentication failed"):
        super().__init__(message, status.HTTP_401_UNAUTHORIZED)


class InvalidTokenError(AppException):
    def __init__(self, message: str = "Invalid or expired token"):
        super().__init__(message, status.HTTP_401_UNAUTHORIZED)


class PermissionDeniedError(AppException):
    def __init__(self, message: str = "You do not have permission to perform this action"):
        super().__init__(message, status.HTTP_403_FORBIDDEN)


class NotFoundError(AppException):
    def __init__(self, resource: str = "Resource", message: Optional[str] = None):
        super().__init__(
            message or f"{resource} not found",
            status.HTTP_404_NOT_FOUND,
        )


class AlreadyExistsError(AppException):
    def __init__(self, resource: str = "Resource", message: Optional[str] = None):
        super().__init__(
            message or f"{resource} already exists",
            status.HTTP_409_CONFLICT,
        )


class ValidationError_(AppException):
    def __init__(self, message: str = "Validation error", errors: Optional[List] = None):
        super().__init__(message, status.HTTP_422_UNPROCESSABLE_ENTITY, errors)


class BadRequestError(AppException):
    def __init__(self, message: str = "Bad request"):
        super().__init__(message, status.HTTP_400_BAD_REQUEST)


class RateLimitError(AppException):
    def __init__(self, message: str = "Rate limit exceeded"):
        super().__init__(message, status.HTTP_429_TOO_MANY_REQUESTS)


class ServiceUnavailableError(AppException):
    def __init__(self, service: str = "Service"):
        super().__init__(f"{service} is currently unavailable", status.HTTP_503_SERVICE_UNAVAILABLE)


def _error_response(
    status_code: int,
    message: str,
    errors: Optional[List] = None,
) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={
            "success": False,
            "message": message,
            "data": None,
            "errors": errors or [],
        },
    )


def register_exception_handlers(app: FastAPI) -> None:
    """Register all custom exception handlers on the FastAPI app."""

    @app.exception_handler(AppException)
    async def app_exception_handler(request: Request, exc: AppException):
        return _error_response(exc.status_code, exc.message, exc.errors)

    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(request: Request, exc: StarletteHTTPException):
        return _error_response(exc.status_code, str(exc.detail))

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(request: Request, exc: RequestValidationError):
        errors = [
            {"field": ".".join(str(loc) for loc in e["loc"]), "message": e["msg"]}
            for e in exc.errors()
        ]
        return _error_response(
            status.HTTP_422_UNPROCESSABLE_ENTITY,
            "Request validation failed",
            errors,
        )

    @app.exception_handler(JWTError)
    async def jwt_exception_handler(request: Request, exc: JWTError):
        return _error_response(status.HTTP_401_UNAUTHORIZED, "Invalid or expired token")

    @app.exception_handler(Exception)
    async def generic_exception_handler(request: Request, exc: Exception):
        return _error_response(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            "An internal server error occurred",
        )
