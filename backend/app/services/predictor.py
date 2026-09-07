from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

import joblib
import pandas as pd

from app.schemas import (
    FarePredictionRequest,
    ProviderPrediction,
)
from app.services.zone_resolver import taxi_zone_resolver


ROOT = Path(__file__).resolve().parents[2]
ARTIFACT_DIR = ROOT / "ml" / "artifacts"

AIRPORT_ZONE_IDS = {1, 132, 138}


@dataclass
class LoadedModel:
    pipeline: object
    mae: float
    median_error: float
    error_80: float


class FarePredictor:
    PROVIDERS = ("uber", "lyft")

    def __init__(self) -> None:
        self.models: dict[str, LoadedModel] = {}
        self.reload()

    def reload(self) -> None:
        self.models.clear()

        for provider in self.PROVIDERS:
            model_path = (
                ARTIFACT_DIR
                / f"{provider}_fare_model.joblib"
            )

            metrics_path = (
                ARTIFACT_DIR
                / f"{provider}_metrics.json"
            )

            if (
                not model_path.exists()
                or not metrics_path.exists()
            ):
                continue

            pipeline = joblib.load(
                model_path
            )

            metrics = json.loads(
                metrics_path.read_text()
            )

            self.models[provider] = LoadedModel(
                pipeline=pipeline,
                mae=float(metrics["mae"]),
                median_error=float(
                    metrics.get(
                        "median_absolute_error",
                        metrics["mae"],
                    )
                ),
                error_80=float(
                    metrics.get(
                        "error_80_percentile",
                        metrics["mae"],
                    )
                ),
            )

    @property
    def loaded_providers(self) -> list[str]:
        return sorted(
            self.models.keys()
        )

    def _build_model_row(
        self,
        request: FarePredictionRequest,
    ) -> pd.DataFrame:

        pickup_zone = (
            taxi_zone_resolver.resolve(
                request.pickup_lat,
                request.pickup_lon,
            )
        )

        dropoff_zone = (
            taxi_zone_resolver.resolve(
                request.dropoff_lat,
                request.dropoff_lon,
            )
        )

        is_weekend = int(
            request.day_of_week >= 5
        )

        is_rush_hour = int(
            7 <= request.pickup_hour <= 9
            or 16 <= request.pickup_hour <= 19
        )

        is_late_night = int(
            request.pickup_hour >= 22
            or request.pickup_hour <= 4
        )

        is_airport_trip = int(
            pickup_zone in AIRPORT_ZONE_IDS
            or dropoff_zone in AIRPORT_ZONE_IDS
        )

        average_speed_mph = (
            request.trip_miles
            / (
                request.trip_minutes
                / 60.0
            )
        )

        average_speed_mph = max(
            1.0,
            min(
                80.0,
                average_speed_mph,
            ),
        )

        pickup_zone_str = str(
            pickup_zone
        )

        dropoff_zone_str = str(
            dropoff_zone
        )

        route_pair = (
            f"{pickup_zone_str}_"
            f"{dropoff_zone_str}"
        )

        return pd.DataFrame(
            [
                {
                    "trip_miles":
                        request.trip_miles,

                    "trip_minutes":
                        request.trip_minutes,

                    "average_speed_mph":
                        average_speed_mph,

                    "pickup_hour":
                        request.pickup_hour,

                    "day_of_week":
                        request.day_of_week,

                    "is_weekend":
                        is_weekend,

                    "is_rush_hour":
                        is_rush_hour,

                    "is_late_night":
                        is_late_night,

                    "is_airport_trip":
                        is_airport_trip,

                    "PULocationID":
                        pickup_zone_str,

                    "DOLocationID":
                        dropoff_zone_str,

                    "route_pair":
                        route_pair,
                }
            ]
        )

    def predict(
        self,
        request: FarePredictionRequest,
    ) -> list[ProviderPrediction]:

        if not self.models:
            raise RuntimeError(
                "No trained models found. "
                "Run the training pipeline first."
            )

        row = self._build_model_row(
            request
        )

        output: list[
            ProviderPrediction
        ] = []

        for provider in self.PROVIDERS:
            loaded = self.models.get(
                provider
            )

            if loaded is None:
                continue

            estimate = float(
                loaded.pipeline.predict(
                    row
                )[0]
            )

            estimate = max(
                0.0,
                estimate,
            )

            lower = max(
                0.0,
                estimate
                - loaded.error_80,
            )

            upper = (
                estimate
                + loaded.error_80
            )

            output.append(
                ProviderPrediction(
                    provider=provider,

                    estimated_fare=round(
                        estimate,
                        2,
                    ),

                    lower_bound=round(
                        lower,
                        2,
                    ),

                    upper_bound=round(
                        upper,
                        2,
                    ),

                    model_mae=round(
                        loaded.mae,
                        2,
                    ),

                    median_error=round(
                        loaded.median_error,
                        2,
                    ),

                    error_80_percentile=round(
                        loaded.error_80,
                        2,
                    ),
                )
            )

        return output

    def predict_for_time(
        self,
        request: FarePredictionRequest,
        pickup_hour: int,
        day_of_week: int,
    ) -> list[ProviderPrediction]:

        adjusted_request = (
            FarePredictionRequest(
                trip_miles=request.trip_miles,
                trip_minutes=request.trip_minutes,

                pickup_hour=pickup_hour,
                day_of_week=day_of_week,

                pickup_lat=request.pickup_lat,
                pickup_lon=request.pickup_lon,

                dropoff_lat=request.dropoff_lat,
                dropoff_lon=request.dropoff_lon,
            )
        )

        return self.predict(
            adjusted_request
        )