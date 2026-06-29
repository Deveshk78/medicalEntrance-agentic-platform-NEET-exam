from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.config import settings
from app.routers import analytics, predictions, reports
from app.services.ml_engine import MLEngine
from app.services.data_store import DataStore


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.ml_engine = MLEngine()
    app.state.ml_engine.build_models()
    app.state.data_store = DataStore()
    await app.state.data_store.connect()
    yield
    await app.state.data_store.disconnect()


app = FastAPI(
    title="MedEnt Analytics API",
    description="Comprehensive analytics and Keras ML predictions for medical entrance exams",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analytics.router, prefix="/api/v1/analytics", tags=["Analytics"])
app.include_router(predictions.router, prefix="/api/v1/predictions", tags=["Predictions"])
app.include_router(reports.router, prefix="/api/v1/reports", tags=["Reports"])


@app.get("/health")
async def health():
    return {"status": "UP", "service": "analytics-api"}
