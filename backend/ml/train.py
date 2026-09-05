"""Train Uber and Lyft fare models from NYC TLC HVFHV trip data.

Public TLC HVFHV provider IDs:
- HV0003 = Uber
- HV0005 = Lyft

Target:
    base_passenger_fare

Example:
    python -m ml.train \
        --input data/fhvhv_tripdata_2025-01.parquet \
        --max-rows 1500000
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
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from xgboost import XGBRegressor


ROOT = Path(__file__).resolve().parents[1]
ARTIFACT_DIR = ROOT / "ml" / "artifacts"

PROVIDERS = {
    "uber": "HV0003",
    "lyft": "HV0005",
}

# NYC TLC airport taxi-zone IDs:
# 1   = Newark Airport
# 132 = JFK Airport
# 138 = LaGuardia Airport
AIRPORT_ZONE_IDS = {1, 132, 138}


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
    "average_speed_mph",
    "pickup_hour",
    "day_of_week",
    "is_weekend",
    "is_rush_hour",
    "is_late_night",
    "is_airport_trip",
]


CATEGORICAL_FEATURES = [
    "PULocationID",
    "DOLocationID",
    "route_pair",
]


FEATURES = NUMERIC_FEATURES + CATEGORICAL_FEATURES


def prepare_frame(path: Path, max_rows: int | None) -> pd.DataFrame:
    print("Loading TLC dataset...")

    df = pd.read_parquet(path, columns=RAW_COLUMNS)

    if max_rows and len(df) > max_rows:
        print(f"Sampling {max_rows:,} rows from {len(df):,} total rows...")
        df = df.sample(n=max_rows, random_state=42)

    # Required fields
    df = df.dropna(subset=RAW_COLUMNS)

    # Datetimes
    df["pickup_datetime"] = pd.to_datetime(
        df["pickup_datetime"],
        errors="coerce",
    )

    df["dropoff_datetime"] = pd.to_datetime(
        df["dropoff_datetime"],
        errors="coerce",
    )

    df = df.dropna(subset=["pickup_datetime", "dropoff_datetime"])

    # Numeric conversion
    df["trip_minutes"] = (
        pd.to_numeric(df["trip_time"], errors="coerce") / 60.0
    )

    df["trip_miles"] = pd.to_numeric(
        df["trip_miles"],
        errors="coerce",
    )

    df["base_passenger_fare"] = pd.to_numeric(
        df["base_passenger_fare"],
        errors="coerce",
    )

    df["PULocationID"] = pd.to_numeric(
        df["PULocationID"],
        errors="coerce",
    )

    df["DOLocationID"] = pd.to_numeric(
        df["DOLocationID"],
        errors="coerce",
    )

    df = df.dropna(
        subset=[
            "trip_minutes",
            "trip_miles",
            "base_passenger_fare",
            "PULocationID",
            "DOLocationID",
        ]
    )

    # Remove corrupted / unrealistic outliers.
    df = df[
        df["trip_miles"].between(0.2, 100)
        & df["trip_minutes"].between(1, 240)
        & df["base_passenger_fare"].between(2, 500)
    ].copy()

    # ---------------------------------------------------------
    # Time features
    # ---------------------------------------------------------

    df["pickup_hour"] = df["pickup_datetime"].dt.hour.astype(int)

    df["day_of_week"] = (
        df["pickup_datetime"]
        .dt.dayofweek
        .astype(int)
    )

    df["is_weekend"] = (
        df["day_of_week"] >= 5
    ).astype(int)

    # NYC commuting windows.
    df["is_rush_hour"] = (
        df["pickup_hour"].between(7, 9)
        | df["pickup_hour"].between(16, 19)
    ).astype(int)

    # Late-night demand pattern.
    df["is_late_night"] = (
        (df["pickup_hour"] >= 22)
        | (df["pickup_hour"] <= 4)
    ).astype(int)

    # ---------------------------------------------------------
    # Trip behavior features
    # ---------------------------------------------------------

    df["average_speed_mph"] = (
        df["trip_miles"]
        / (df["trip_minutes"] / 60.0)
    )

    # Protect against weird GPS/time records.
    df["average_speed_mph"] = (
        df["average_speed_mph"]
        .clip(lower=1, upper=80)
    )

    # ---------------------------------------------------------
    # Geographic features
    # ---------------------------------------------------------

    df["is_airport_trip"] = (
        df["PULocationID"].isin(AIRPORT_ZONE_IDS)
        | df["DOLocationID"].isin(AIRPORT_ZONE_IDS)
    ).astype(int)

    # Convert zones to categorical strings.
    df["PULocationID"] = (
        df["PULocationID"]
        .astype(int)
        .astype(str)
    )

    df["DOLocationID"] = (
        df["DOLocationID"]
        .astype(int)
        .astype(str)
    )

    # Route pair captures interactions such as:
    # Manhattan -> JFK
    # Brooklyn -> Manhattan
    # Queens -> LaGuardia
    df["route_pair"] = (
        df["PULocationID"]
        + "_"
        + df["DOLocationID"]
    )

    return df


def build_pipeline() -> Pipeline:
    preprocessor = ColumnTransformer(
        transformers=[
            (
                "numeric",
                "passthrough",
                NUMERIC_FEATURES,
            ),
            (
                "categorical",
                OneHotEncoder(
                    handle_unknown="ignore",
                    sparse_output=True,
                    min_frequency=10,
                ),
                CATEGORICAL_FEATURES,
            ),
        ]
    )

    model = XGBRegressor(
        n_estimators=650,
        max_depth=9,
        learning_rate=0.045,
        subsample=0.90,
        colsample_bytree=0.90,
        min_child_weight=5,
        reg_alpha=0.05,
        reg_lambda=1.0,
        objective="reg:squarederror",
        eval_metric="mae",
        random_state=42,
        n_jobs=-1,
        tree_method="hist",
    )

    return Pipeline(
        [
            ("preprocess", preprocessor),
            ("model", model),
        ]
    )


def get_feature_importance(
    pipeline: Pipeline,
    top_n: int = 20,
) -> list[dict]:
    """Return the most important transformed model features."""

    preprocessor = pipeline.named_steps["preprocess"]
    model = pipeline.named_steps["model"]

    feature_names = preprocessor.get_feature_names_out()
    importances = model.feature_importances_

    feature_df = pd.DataFrame(
        {
            "feature": feature_names,
            "importance": importances,
        }
    )

    feature_df = feature_df.sort_values(
        "importance",
        ascending=False,
    ).head(top_n)

    return [
        {
            "feature": str(row["feature"]),
            "importance": float(row["importance"]),
        }
        for _, row in feature_df.iterrows()
    ]


def train_provider(
    df: pd.DataFrame,
    provider: str,
    license_num: str,
) -> dict:
    provider_df = df[
        df["hvfhs_license_num"] == license_num
    ].copy()

    if len(provider_df) < 1000:
        raise ValueError(
            f"Only {len(provider_df)} usable {provider} rows found. "
            "Use a larger monthly file/sample."
        )

    # ---------------------------------------------------------
    # Time-based split
    #
    # Earlier 80% -> training
    # Latest 20%  -> testing
    #
    # This is more realistic than randomly mixing future and
    # past trips together.
    # ---------------------------------------------------------

    provider_df = provider_df.sort_values(
        "pickup_datetime"
    ).reset_index(drop=True)

    split_index = int(len(provider_df) * 0.80)

    train_df = provider_df.iloc[:split_index]
    test_df = provider_df.iloc[split_index:]

    X_train = train_df[FEATURES]
    y_train = train_df["base_passenger_fare"]

    X_test = test_df[FEATURES]
    y_test = test_df["base_passenger_fare"]

    print()
    print(
        f"Training {provider.title()} "
        f"on {len(X_train):,} trips..."
    )

    pipeline = build_pipeline()

    pipeline.fit(
        X_train,
        y_train,
    )

    predictions = pipeline.predict(X_test)

    mae = mean_absolute_error(
        y_test,
        predictions,
    )

    rmse = np.sqrt(
        mean_squared_error(
            y_test,
            predictions,
        )
    )

    r2 = r2_score(
        y_test,
        predictions,
    )

    # Prediction error percentiles are useful for building
    # realistic prediction intervals in the Android app.
    absolute_errors = np.abs(
        y_test.to_numpy() - predictions
    )

    error_p50 = float(
        np.percentile(
            absolute_errors,
            50,
        )
    )

    error_p80 = float(
        np.percentile(
            absolute_errors,
            80,
        )
    )

    error_p90 = float(
        np.percentile(
            absolute_errors,
            90,
        )
    )

    feature_importance = get_feature_importance(
        pipeline,
        top_n=20,
    )

    metrics = {
        "provider": provider,
        "license_num": license_num,
        "rows": int(len(provider_df)),
        "train_rows": int(len(X_train)),
        "test_rows": int(len(X_test)),
        "mae": float(mae),
        "rmse": float(rmse),
        "r2": float(r2),
        "median_absolute_error": error_p50,
        "error_80_percentile": error_p80,
        "error_90_percentile": error_p90,
        "target": "base_passenger_fare",
        "features": FEATURES,
        "feature_importance": feature_importance,
        "validation_strategy": "chronological_80_20_split",
    }

    ARTIFACT_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    # Save trained model.
    joblib.dump(
        pipeline,
        ARTIFACT_DIR
        / f"{provider}_fare_model.joblib",
    )

    # Save evaluation results.
    metrics_path = (
        ARTIFACT_DIR
        / f"{provider}_metrics.json"
    )

    metrics_path.write_text(
        json.dumps(
            metrics,
            indent=2,
        )
    )

    print(
        f"{provider.title()} results:"
    )

    print(
        f"  MAE:  ${mae:.2f}"
    )

    print(
        f"  RMSE: ${rmse:.2f}"
    )

    print(
        f"  R²:   {r2:.3f}"
    )

    print(
        f"  Median error: ${error_p50:.2f}"
    )

    print(
        f"  80% of predictions within approximately "
        f"${error_p80:.2f}"
    )

    print()
    print("Top model features:")

    for item in feature_importance[:10]:
        print(
            f"  {item['feature']:<45} "
            f"{item['importance']:.4f}"
        )

    return metrics


def main() -> None:
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--input",
        type=Path,
        required=True,
    )

    parser.add_argument(
        "--max-rows",
        type=int,
        default=500_000,
        help=(
            "Randomly sample at most this many rows "
            "before training; use 0 for all rows."
        ),
    )

    args = parser.parse_args()

    max_rows = (
        args.max_rows
        if args.max_rows > 0
        else None
    )

    frame = prepare_frame(
        args.input,
        max_rows,
    )

    print(
        f"Prepared {len(frame):,} usable rows"
    )

    results = []

    for provider, license_num in PROVIDERS.items():
        metrics = train_provider(
            frame,
            provider,
            license_num,
        )

        results.append(metrics)

    print()
    print("=" * 60)
    print("FINAL MODEL RESULTS")
    print("=" * 60)

    for metrics in results:
        print(
            f"{metrics['provider'].title():<8} "
            f"rows={metrics['rows']:,} | "
            f"MAE=${metrics['mae']:.2f} | "
            f"RMSE=${metrics['rmse']:.2f} | "
            f"R²={metrics['r2']:.3f}"
        )


if __name__ == "__main__":
    main()