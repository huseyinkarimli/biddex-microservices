"""
Biddex analytics-service: FastAPI skeleton with scikit-learn win-probability scoring.
"""

from __future__ import annotations

import hashlib
import uuid

import numpy as np
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from sklearn.linear_model import LogisticRegression

app = FastAPI(
    title="Biddex Analytics Service",
    version="0.1.0",
    description="Analytics and ML helpers for the Biddex B2B tender platform.",
)


class WinProbabilityRequest(BaseModel):
    """Input features for a simple logistic win-probability model."""

    tender_id: str | None = Field(default=None, description="Optional tender identifier")
    bid_amount: float = Field(..., ge=0, description="Submitted bid amount")
    estimated_cost: float = Field(..., ge=0, description="Bidder internal cost estimate")
    competitor_count: int = Field(..., ge=0, description="Expected number of competitors")
    historical_win_rate: float = Field(
        ...,
        ge=0,
        le=1,
        description="Company historical win rate in similar tenders (0-1)",
    )


class WinProbabilityResponse(BaseModel):
    tender_id: str | None
    win_probability: float = Field(..., ge=0, le=1)
    model_version: str
    request_id: str


class CompanyReportResponse(BaseModel):
    company_id: str
    period_days: int
    tenders_participated: int
    estimated_win_rate: float
    revenue_band: str
    notes: str


def _build_model() -> LogisticRegression:
    """
    Train a small logistic regression on synthetic data so the service is runnable
    without an external training pipeline.
    """
    rng = np.random.default_rng(42)
    n = 800
    bid_ratio = rng.uniform(0.85, 1.15, size=n)
    competitors = rng.integers(1, 12, size=n)
    hist = rng.uniform(0.05, 0.65, size=n)
    noise = rng.normal(0, 0.08, size=n)
    score = (
        2.4 * (1.0 - bid_ratio)
        + -0.35 * competitors
        + 2.1 * hist
        + noise
    )
    won = (score > np.median(score)).astype(int)
    X = np.column_stack([bid_ratio, competitors.astype(float), hist])
    model = LogisticRegression(max_iter=500)
    model.fit(X, won)
    return model


_MODEL = _build_model()
_MODEL_VERSION = "logreg-synthetic-v1"


def _features_from_request(body: WinProbabilityRequest) -> np.ndarray:
    if body.estimated_cost <= 0:
        raise HTTPException(status_code=400, detail="estimated_cost must be positive")
    bid_ratio = float(body.bid_amount / body.estimated_cost)
    row = np.array([[bid_ratio, float(body.competitor_count), float(body.historical_win_rate)]])
    return row


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP", "service": "analytics-service"}


@app.post("/analytics/win-probability", response_model=WinProbabilityResponse)
def win_probability(body: WinProbabilityRequest) -> WinProbabilityResponse:
    X = _features_from_request(body)
    proba = float(_MODEL.predict_proba(X)[0, 1])
    proba = max(0.0, min(1.0, proba))
    return WinProbabilityResponse(
        tender_id=body.tender_id,
        win_probability=proba,
        model_version=_MODEL_VERSION,
        request_id=str(uuid.uuid4()),
    )


@app.get("/analytics/company/{company_id}/report", response_model=CompanyReportResponse)
def company_report(company_id: str, period_days: int = 90) -> CompanyReportResponse:
    if not company_id.strip():
        raise HTTPException(status_code=400, detail="company_id is required")
    if period_days < 1 or period_days > 3650:
        raise HTTPException(status_code=400, detail="period_days must be between 1 and 3650")

    digest = hashlib.sha256(company_id.encode("utf-8")).hexdigest()
    seed = int(digest[:8], 16)
    rng = np.random.default_rng(seed)
    participated = int(rng.integers(3, 80))
    win_rate = float(rng.uniform(0.12, 0.55))
    band_roll = float(rng.uniform(0, 1))
    if band_roll < 0.33:
        revenue_band = "low"
    elif band_roll < 0.66:
        revenue_band = "medium"
    else:
        revenue_band = "high"

    summary = pd.DataFrame(
        {
            "metric": ["tenders_participated", "estimated_win_rate"],
            "value": [float(participated), win_rate],
        }
    )
    blend = float(summary["value"].mean())

    return CompanyReportResponse(
        company_id=company_id,
        period_days=period_days,
        tenders_participated=participated,
        estimated_win_rate=round(win_rate, 4),
        revenue_band=revenue_band,
        notes=(
            "Synthetic summary for development; replace with SQL / Kafka aggregates in production. "
            f"Blended score (pandas): {blend:.4f}."
        ),
    )


