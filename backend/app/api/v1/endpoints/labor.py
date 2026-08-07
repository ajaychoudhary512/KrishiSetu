"""
AgriLink AI — Labour Hiring Endpoints
"""
from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter(prefix="/labor", tags=["Labour Hiring"])

class JobPosting(BaseModel):
    title: str
    workers_needed: int
    wage: str
    location: str
    crop_type: str

DEMO_JOBS = [
    {
        "id": 101,
        "title": "Paddy Harvesting Workers Needed",
        "workers_needed": 6,
        "wage": "₹650 / Day",
        "location": "Sangrur, Punjab",
        "crop_type": "Paddy / Rice",
        "posted_by": "Balwinder Singh"
    },
    {
        "id": 102,
        "title": "Sugarcane Cutting & Loading Team",
        "workers_needed": 12,
        "wage": "₹700 / Day",
        "location": "Meerut, UP",
        "crop_type": "Sugarcane",
        "posted_by": "Choudhary Agro Farms"
    }
]

@router.get("", summary="Get labour job openings")
async def get_labor_jobs():
    return {"status": "success", "data": DEMO_JOBS}

@router.post("/job", status_code=status.HTTP_201_CREATED, summary="Post a new labour job requirement")
async def create_job_posting(job: JobPosting):
    item = job.model_dump()
    item["id"] = len(DEMO_JOBS) + 101
    item["posted_by"] = "Current Farmer User"
    DEMO_JOBS.insert(0, item)
    return {"status": "success", "message": "Job requirement posted successfully", "data": item}

@router.post("/apply/{job_id}", summary="Apply for a labour job")
async def apply_for_job(job_id: int):
    return {"status": "success", "message": f"Application submitted for Job #{job_id}!"}
