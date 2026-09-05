from typing import Literal

from pydantic import BaseModel, Field, model_validator


ProviderName = Literal["uber", "lyft"]


class FarePredictionRequest(BaseModel):
    trip_miles: float = Field(gt=0, le=200)
    trip_minutes: float = Field(gt=0, le=360)
    pickup_hour: int = Field(ge=0, le=23)
    day_of_week: int = Field(ge=0, le=6, description="Monday=0, Sunday=6")
    pickup_zone_id: int = Field(ge=1, le=999)
    dropoff_zone_id: int = Field(ge=1, le=999)


class ProviderPrediction(BaseModel):
    provider: ProviderName
    estimated_fare: float
    lower_bound: float
    upper_bound: float
    model_mae: float


class FarePredictionResponse(BaseModel):
    market: str = "NYC"
    currency: str = "USD"
    data_basis: str = "NYC TLC High Volume FHV historical trips"
    predictions: list[ProviderPrediction]


class HealthResponse(BaseModel):
    status: Literal["ok", "degraded"]
    models_loaded: list[ProviderName]
