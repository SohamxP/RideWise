from datetime import datetime
from zoneinfo import ZoneInfo

from fastapi import FastAPI, HTTPException

from app.schemas import (
    AnalyzeTripRequest,
    AnalyzeTripResponse,
    FarePredictionRequest,
    FarePredictionResponse,
    HealthResponse,
    RouteInfo,
)
from app.services.predictor import FarePredictor
from app.services.route_service import route_service


app = FastAPI(
    title="RideWise Prediction API",
    version="0.2.0",
    description=(
        "Route-aware provider-specific historical fare predictions "
        "trained from public NYC TLC High Volume For-Hire Vehicle trip records."
    ),
)

predictor = FarePredictor()


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    loaded = predictor.loaded_providers

    return HealthResponse(
        status="ok" if len(loaded) == 2 else "degraded",
        models_loaded=loaded,
    )


@app.post(
    "/v1/predict-fares",
    response_model=FarePredictionResponse,
)
def predict_fares(
    request: FarePredictionRequest,
) -> FarePredictionResponse:
    try:
        predictions = predictor.predict(request)

    except RuntimeError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc

    except ValueError as exc:
        raise HTTPException(
            status_code=422,
            detail=str(exc),
        ) from exc

    return FarePredictionResponse(
        predictions=predictions,
    )


@app.post(
    "/v1/analyze-trip",
    response_model=AnalyzeTripResponse,
)
async def analyze_trip(
    request: AnalyzeTripRequest,
) -> AnalyzeTripResponse:

    # ---------------------------------------------------------
    # 1. Ask Google Routes for actual driving distance/time
    # ---------------------------------------------------------

    try:
        route = await route_service.get_route(
            pickup_lat=request.pickup_lat,
            pickup_lon=request.pickup_lon,
            dropoff_lat=request.dropoff_lat,
            dropoff_lon=request.dropoff_lon,
        )

    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Route lookup failed: {exc}",
        ) from exc

    # ---------------------------------------------------------
    # 2. Determine current NYC time automatically
    # ---------------------------------------------------------

    now_nyc = datetime.now(
        ZoneInfo("America/New_York")
    )

    pickup_hour = now_nyc.hour
    day_of_week = now_nyc.weekday()

    # ---------------------------------------------------------
    # 3. Build the model request using REAL route information
    # ---------------------------------------------------------

    prediction_request = FarePredictionRequest(
        trip_miles=route["trip_miles"],
        trip_minutes=route["trip_minutes"],
        pickup_hour=pickup_hour,
        day_of_week=day_of_week,
        pickup_lat=request.pickup_lat,
        pickup_lon=request.pickup_lon,
        dropoff_lat=request.dropoff_lat,
        dropoff_lon=request.dropoff_lon,
    )

    # ---------------------------------------------------------
    # 4. Run Uber + Lyft models
    # ---------------------------------------------------------

    try:
        predictions = predictor.predict(
            prediction_request
        )

    except RuntimeError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc

    except ValueError as exc:
        raise HTTPException(
            status_code=422,
            detail=str(exc),
        ) from exc

    # ---------------------------------------------------------
    # 5. Return one clean response to Android
    # ---------------------------------------------------------

    return AnalyzeTripResponse(
        route=RouteInfo(
            trip_miles=route["trip_miles"],
            trip_minutes=route["trip_minutes"],
        ),
        predictions=predictions,
    )