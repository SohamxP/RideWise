from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

import joblib
import pandas as pd

from app.schemas import FarePredictionRequest, ProviderPrediction


ROOT = Path(__file__).resolve().parents[2]
ARTIFACT_DIR = ROOT / "ml" / "artifacts"


@dataclass
class LoadedModel:
    pipeline: object
    mae: float


class FarePredictor:
    """Loads provider-specific models trained from NYC TLC HVFHV trip records."""

    PROVIDERS = ("uber", "lyft")

    def __init__(self) -> None:
        self.models: dict[str, LoadedModel] = {}
        self.reload()

    def reload(self) -> None:
        self.models.clear()
        for provider in self.PROVIDERS:
            model_path = ARTIFACT_DIR / f"{provider}_fare_model.joblib"
            metrics_path = ARTIFACT_DIR / f"{provider}_metrics.json"
            if not model_path.exists() or not metrics_path.exists():
                continue

            pipeline = joblib.load(model_path)
            metrics = json.loads(metrics_path.read_text())
            self.models[provider] = LoadedModel(
                pipeline=pipeline,
                mae=float(metrics["mae"]),
            )

    @property
    def loaded_providers(self) -> list[str]:
        return sorted(self.models.keys())

    def predict(self, request: FarePredictionRequest) -> list[ProviderPrediction]:
        if not self.models:
            raise RuntimeError(
                "No trained models found. Run `python -m ml.train --input <parquet>` first."
            )

        row = pd.DataFrame(
            [
                {
                    "trip_miles": request.trip_miles,
                    "trip_minutes": request.trip_minutes,
                    "pickup_hour": request.pickup_hour,
                    "day_of_week": request.day_of_week,
                    "is_weekend": int(request.day_of_week >= 5),
                    "PULocationID": str(request.pickup_zone_id),
                    "DOLocationID": str(request.dropoff_zone_id),
                }
            ]
        )

        output: list[ProviderPrediction] = []
        for provider in self.PROVIDERS:
            loaded = self.models.get(provider)
            if loaded is None:
                continue

            estimate = max(0.0, float(loaded.pipeline.predict(row)[0]))
            # Use holdout MAE as an interpretable empirical uncertainty band.
            lower = max(0.0, estimate - loaded.mae)
            upper = estimate + loaded.mae
            output.append(
                ProviderPrediction(
                    provider=provider,
                    estimated_fare=round(estimate, 2),
                    lower_bound=round(lower, 2),
                    upper_bound=round(upper, 2),
                    model_mae=round(loaded.mae, 2),
                )
            )

        return output
