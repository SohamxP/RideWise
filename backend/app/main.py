from fastapi import FastAPI, HTTPException

from app.schemas import FarePredictionRequest, FarePredictionResponse, HealthResponse
from app.services.predictor import FarePredictor


app = FastAPI(
    title="RideWise Prediction API",
    version="0.1.0",
    description=(
        "Provider-specific historical fare predictions trained from public NYC TLC "
        "High Volume For-Hire Vehicle trip records."
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


@app.post("/v1/predict-fares", response_model=FarePredictionResponse)
def predict_fares(request: FarePredictionRequest) -> FarePredictionResponse:
    try:
        predictions = predictor.predict(request)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

    return FarePredictionResponse(predictions=predictions)
