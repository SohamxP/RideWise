"""Train Uber and Lyft fare models from NYC TLC HVFHV trip data.

The public dataset identifies providers by HVFHS license number:
- HV0003 = Uber
- HV0005 = Lyft

Target: base_passenger_fare (before tolls, tips, taxes and fees).

Example:
    python -m ml.train --input data/fhvhv_tripdata_2025-01.parquet --max-rows 500000
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from xgboost import XGBRegressor


ROOT = Path(__file__).resolve().parents[1]
ARTIFACT_DIR = ROOT / "ml" / "artifacts"

PROVIDERS = {
    "uber": "HV0003",
    "lyft": "HV0005",
}

RAW_COLUMNS = [
    "hvfhs_license_num",
    "pickup_datetime",
    "dropoff_datetime",
    "PULocationID",
    "DOLocationID",
    "trip_miles",
    "trip_time",
    "base_passenger_fare",
]

NUMERIC_FEATURES = [
    "trip_miles",
    "trip_minutes",
    "pickup_hour",
    "day_of_week",
    "is_weekend",
]

CATEGORICAL_FEATURES = ["PULocationID", "DOLocationID"]
FEATURES = NUMERIC_FEATURES + CATEGORICAL_FEATURES


def prepare_frame(path: Path, max_rows: int | None) -> pd.DataFrame:
    df = pd.read_parquet(path, columns=RAW_COLUMNS)

    if max_rows and len(df) > max_rows:
        df = df.sample(n=max_rows, random_state=42)

    df = df.dropna(subset=RAW_COLUMNS)
    df["pickup_datetime"] = pd.to_datetime(df["pickup_datetime"], errors="coerce")
    df["dropoff_datetime"] = pd.to_datetime(df["dropoff_datetime"], errors="coerce")
    df = df.dropna(subset=["pickup_datetime", "dropoff_datetime"])

    df["trip_minutes"] = pd.to_numeric(df["trip_time"], errors="coerce") / 60.0
    df["trip_miles"] = pd.to_numeric(df["trip_miles"], errors="coerce")
    df["base_passenger_fare"] = pd.to_numeric(df["base_passenger_fare"], errors="coerce")

    # Remove clearly invalid/extreme records that distort a student-scale model.
    df = df[
        df["trip_miles"].between(0.2, 100)
        & df["trip_minutes"].between(1, 240)
        & df["base_passenger_fare"].between(2, 500)
    ].copy()

    df["pickup_hour"] = df["pickup_datetime"].dt.hour.astype(int)
    df["day_of_week"] = df["pickup_datetime"].dt.dayofweek.astype(int)
    df["is_weekend"] = (df["day_of_week"] >= 5).astype(int)
    df["PULocationID"] = df["PULocationID"].astype(int).astype(str)
    df["DOLocationID"] = df["DOLocationID"].astype(int).astype(str)
    return df


def build_pipeline() -> Pipeline:
    preprocessor = ColumnTransformer(
        transformers=[
            ("numeric", "passthrough", NUMERIC_FEATURES),
            (
                "zones",
                OneHotEncoder(handle_unknown="ignore", sparse_output=True),
                CATEGORICAL_FEATURES,
            ),
        ]
    )

    model = XGBRegressor(
        n_estimators=500,
        max_depth=8,
        learning_rate=0.05,
        subsample=0.85,
        colsample_bytree=0.85,
        objective="reg:squarederror",
        eval_metric="mae",
        random_state=42,
        n_jobs=-1,
    )

    return Pipeline([("preprocess", preprocessor), ("model", model)])


def train_provider(df: pd.DataFrame, provider: str, license_num: str) -> dict:
    provider_df = df[df["hvfhs_license_num"] == license_num].copy()
    if len(provider_df) < 1000:
        raise ValueError(
            f"Only {len(provider_df)} usable {provider} rows found. Use a larger monthly file/sample."
        )

    X = provider_df[FEATURES]
    y = provider_df["base_passenger_fare"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)
    predictions = pipeline.predict(X_test)

    metrics = {
        "provider": provider,
        "license_num": license_num,
        "rows": int(len(provider_df)),
        "train_rows": int(len(X_train)),
        "test_rows": int(len(X_test)),
        "mae": float(mean_absolute_error(y_test, predictions)),
        "rmse": float(np.sqrt(mean_squared_error(y_test, predictions))),
        "r2": float(r2_score(y_test, predictions)),
        "target": "base_passenger_fare",
        "features": FEATURES,
    }

    ARTIFACT_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(pipeline, ARTIFACT_DIR / f"{provider}_fare_model.joblib")
    (ARTIFACT_DIR / f"{provider}_metrics.json").write_text(
        json.dumps(metrics, indent=2)
    )
    return metrics


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument(
        "--max-rows",
        type=int,
        default=500_000,
        help="Randomly sample at most this many rows before training; use 0 for all rows.",
    )
    args = parser.parse_args()

    max_rows = args.max_rows or None
    frame = prepare_frame(args.input, max_rows)
    print(f"Prepared {len(frame):,} usable rows")

    for provider, license_num in PROVIDERS.items():
        metrics = train_provider(frame, provider, license_num)
        print(
            f"{provider.title()}: rows={metrics['rows']:,}, "
            f"MAE=${metrics['mae']:.2f}, RMSE=${metrics['rmse']:.2f}, R²={metrics['r2']:.3f}"
        )


if __name__ == "__main__":
    main()
