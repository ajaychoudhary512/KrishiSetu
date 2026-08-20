from typing import Optional
from fastapi import APIRouter, status, Query
from pydantic import BaseModel

router = APIRouter(prefix="/labor", tags=["Labour Hiring"])

class JobPosting(BaseModel):
    title: str
    source_type: str = "farmer"  # "farmer" or "industry"
    workers_needed: int
    wage: str
    location: str
    crop_type: str

DEMO_JOBS = [
    {
        "id": 101,
        "title": "🌾 Paddy Harvesting Workers Needed",
        "source_type": "farmer",
        "workers_needed": 6,
        "wage": "₹650 / Day",
        "location": "Sangrur, Punjab",
        "crop_type": "Paddy / Rice",
        "posted_by": "Balwinder Singh (Farmer)"
    },
    {
        "id": 102,
        "title": "🏭 Stubble Pelletizing Factory Crew",
        "source_type": "industry",
        "workers_needed": 12,
        "wage": "₹750 / Day",
        "location": "Pithampur SEZ, MP",
        "crop_type": "Industrial Factory",
        "posted_by": "BioEnergy Processing Ltd (Industry)"
    }
]

@router.get("", summary="Get labour job openings")
async def get_labor_jobs(source_type: Optional[str] = Query(None)):
    results = DEMO_JOBS
    if source_type and source_type.lower() != "all":
        results = [item for item in results if item.get("source_type", "farmer").lower() == source_type.lower()]
    return {"status": "success", "data": results}

@router.post("/job", status_code=status.HTTP_201_CREATED, summary="Post a new labour job requirement")
async def create_job_posting(job: JobPosting):
    item = job.model_dump()
    item["id"] = len(DEMO_JOBS) + 101
    item["posted_by"] = f"Current {job.source_type.capitalize()} User"
    DEMO_JOBS.insert(0, item)
    return {"status": "success", "message": f"{job.source_type.capitalize()} job requirement posted successfully", "data": item}

@router.post("/apply/{job_id}", summary="Apply for a labour job")
async def apply_for_job(job_id: int):
    return {"status": "success", "message": f"Application submitted for Job #{job_id}!"}
