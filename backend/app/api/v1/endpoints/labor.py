from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database.session import get_db
from app.models.labor import LaborJob, WorkerProfile, LaborApplication

router = APIRouter(prefix="/labor", tags=["Labour Hiring"])

class JobPostingRequest(BaseModel):
    title: str = Field(..., min_length=2)
    source_type: str = Field("farmer")  # "farmer" or "industry"
    workers_needed: int = Field(1, ge=1)
    wage: str = Field(...)
    location: str = Field(...)
    crop_type: str = Field("General Farm")
    posted_by: Optional[str] = "Farmer"
    posted_by_phone: Optional[str] = None

class WorkerProfileRequest(BaseModel):
    name: str = Field(..., min_length=2)
    phone: Optional[str] = None
    skills: str = Field("Harvesting, Planting, Irrigation")
    experience: str = Field("3+ Years")
    location: str = Field("Indore, MP")
    daily_rate: float = Field(500.0, gt=0)
    is_available: bool = True

class JobApplicationRequest(BaseModel):
    worker_name: Optional[str] = "Worker"
    worker_phone: Optional[str] = "+919826012345"

SEED_JOBS = [
    {
        "title": "🌾 Paddy Harvesting Workers Needed",
        "source_type": "farmer",
        "workers_needed": 6,
        "wage": "₹650 / Day",
        "location": "Sangrur, Punjab",
        "crop_type": "Paddy / Rice",
        "posted_by": "Balwinder Singh (Farmer)",
        "posted_by_phone": "+919826088888",
        "status": "ACTIVE"
    },
    {
        "title": "🏭 Stubble Pelletizing Factory Crew",
        "source_type": "industry",
        "workers_needed": 12,
        "wage": "₹750 / Day",
        "location": "Pithampur SEZ, MP",
        "crop_type": "Industrial Factory",
        "posted_by": "BioEnergy Processing Ltd (Industry)",
        "posted_by_phone": "+919826099999",
        "status": "ACTIVE"
    }
]

async def ensure_seed_jobs(db: AsyncSession):
    stmt = select(LaborJob).limit(1)
    res = await db.execute(stmt)
    if not res.scalars().first():
        for item in SEED_JOBS:
            db.add(LaborJob(**item))
        await db.commit()

@router.get("", summary="Get labour job openings")
@router.get("/jobs", summary="Get labour job openings (alias)")
async def get_labor_jobs(
    source_type: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    await ensure_seed_jobs(db)
    stmt = select(LaborJob).where(LaborJob.status == "ACTIVE").order_by(LaborJob.id.desc())
    res = await db.execute(stmt)
    items = res.scalars().all()

    results = []
    for item in items:
        if source_type and source_type.lower() != "all":
            if item.source_type.lower() != source_type.lower():
                continue
        results.append(item.to_dict())
    return {"status": "success", "data": results}

@router.post("/job", status_code=status.HTTP_201_CREATED, summary="Post a new labour job requirement")
async def create_job_posting(job: JobPostingRequest, db: AsyncSession = Depends(get_db)):
    item = LaborJob(
        title=job.title,
        source_type=job.source_type,
        workers_needed=job.workers_needed,
        wage=job.wage,
        location=job.location,
        crop_type=job.crop_type,
        posted_by=job.posted_by or "Farmer",
        posted_by_phone=job.posted_by_phone,
        status="ACTIVE"
    )
    db.add(item)
    await db.commit()
    await db.refresh(item)
    return {"status": "success", "message": f"{job.source_type.capitalize()} job requirement posted successfully", "data": item.to_dict()}

@router.post("/apply/{job_id}", summary="Apply for a labour job")
async def apply_for_job(job_id: int, req: JobApplicationRequest, db: AsyncSession = Depends(get_db)):
    job = await db.get(LaborJob, job_id)
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")

    app = LaborApplication(
        job_id=job.id,
        worker_name=req.worker_name or "Worker",
        worker_phone=req.worker_phone,
        status="PENDING"
    )
    db.add(app)
    await db.commit()
    await db.refresh(app)
    return {"status": "success", "message": f"Application submitted for Job #{job_id}!", "data": app.to_dict()}

@router.get("/profile", summary="Get current worker profile")
async def get_worker_profile(db: AsyncSession = Depends(get_db)):
    stmt = select(WorkerProfile).limit(1)
    res = await db.execute(stmt)
    prof = res.scalars().first()
    if not prof:
        prof = WorkerProfile(
            name="Radheshyam Kushwaha",
            phone="+919826012345",
            skills="Harvesting, Machine Operation, Sowing",
            experience="6 Years",
            location="Indore, MP",
            daily_rate=600.0,
            is_available=True,
            completed_jobs=28,
            rating=4.9
        )
        db.add(prof)
        await db.commit()
        await db.refresh(prof)
    return {"status": "success", "data": prof.to_dict()}

@router.put("/profile", summary="Update worker profile and availability")
async def update_worker_profile(req: WorkerProfileRequest, db: AsyncSession = Depends(get_db)):
    stmt = select(WorkerProfile).limit(1)
    res = await db.execute(stmt)
    prof = res.scalars().first()
    if not prof:
        prof = WorkerProfile(name=req.name)
        db.add(prof)

    prof.name = req.name
    if req.phone: prof.phone = req.phone
    prof.skills = req.skills
    prof.experience = req.experience
    prof.location = req.location
    prof.daily_rate = req.daily_rate
    prof.is_available = req.is_available

    await db.commit()
    await db.refresh(prof)
    return {"status": "success", "message": "Worker profile updated", "data": prof.to_dict()}
