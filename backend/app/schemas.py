from typing import Literal

from pydantic import BaseModel, Field


ProviderName = Literal["uber", "lyft"]


class FarePredictionRequest(BaseModel):
    trip_miles: float = Field(gt=0, le=100)
    trip_minutes: float = Field(gt=0, le=240)

    pickup_hour: int = Field(ge=0, le=23)
    day_of_week: int = Field(ge=0, le=6)

    pickup_lat: float = Field(ge=-90, le=90)
    pickup_lon: float = Field(ge=-180, le=180)

    dropoff_lat: float = Field(ge=-90, le=90)
    dropoff_lon: float = Field(ge=-180, le=180)


class ProviderPrediction(BaseModel):
    provider: ProviderName
    estimated_fare: float
    lower_bound: float
    upper_bound: float
    model_mae: float
    median_error: float
    error_80_percentile: float


class FarePredictionResponse(BaseModel):
    market: str = "NYC"
    currency: str = "USD"
    data_basis: str = "NYC TLC High Volume FHV historical trips"
    predictions: list[ProviderPrediction]


class AnalyzeTripRequest(BaseModel):
    pickup_lat: float = Field(ge=-90, le=90)
    pickup_lon: float = Field(ge=-180, le=180)

    dropoff_lat: float = Field(ge=-90, le=90)
    dropoff_lon: float = Field(ge=-180, le=180)


class RouteInfo(BaseModel):
    trip_miles: float
    trip_minutes: float


class AnalyzeTripResponse(BaseModel):
    market: str = "NYC"
    currency: str = "USD"
    data_basis: str = "NYC TLC High Volume FHV historical trips"

    route: RouteInfo
    predictions: list[ProviderPrediction]


class HealthResponse(BaseModel):
    status: Literal["ok", "degraded"]
    models_loaded: list[ProviderName]