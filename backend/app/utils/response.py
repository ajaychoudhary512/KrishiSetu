from typing import Any, Dict, List, Optional

from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse

def success_response(
    data: Any = None,
    message: str = "Success",
    status_code: int = 200,
    meta: Optional[Dict] = None,
) -> JSONResponse:
    
    content = {
        "success": True,
        "message": message,
        "data": data,
        "errors": [],
    }
    if meta:
        content["meta"] = meta
    return JSONResponse(status_code=status_code, content=jsonable_encoder(content))

def error_response(
    message: str = "An error occurred",
    status_code: int = 400,
    errors: Optional[List] = None,
) -> JSONResponse:
    
    return JSONResponse(
        status_code=status_code,
        content=jsonable_encoder({
            "success": False,
            "message": message,
            "data": None,
            "errors": errors or [],
        }),
    )

def paginated_response(
    data: List[Any],
    total: int,
    page: int,
    page_size: int,
    message: str = "Success",
) -> JSONResponse:
    
    total_pages = (total + page_size - 1) // page_size
    return success_response(
        data=data,
        message=message,
        meta={
            "total": total,
            "page": page,
            "page_size": page_size,
            "total_pages": total_pages,
            "has_next": page < total_pages,
            "has_prev": page > 1,
        },
    )
