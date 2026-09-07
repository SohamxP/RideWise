# RideWise Prediction API

Python/FastAPI service for RideWise fare prediction.

## Data source

The training pipeline is designed for the **NYC Taxi & Limousine Commission (TLC) High Volume For-Hire Vehicle (HVFHV) Trip Records**. The public data identifies high-volume providers by license number, including Uber (`HV0003`) and Lyft (`HV0005`), and contains trip miles, trip time, pickup/dropoff taxi zones, and base passenger fare.

The first model is therefore **NYC-calibrated**. Do not describe its output as a real-time provider quote or claim accuracy outside that market without separate evaluation.

## Setup

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Download one official monthly dataset

```bash
python -m ml.download_data --year 2025 --month 1
```

The file is intentionally ignored by Git because the official Parquet files are large.

## Train models

```bash
python -m ml.train \
  --input data/fhvhv_tripdata_2025-01.parquet \
  --max-rows 500000
```

This trains separate Uber and Lyft XGBoost pipelines and writes local model artifacts + evaluation metrics to `ml/artifacts/`.

## Run API

```bash
uvicorn app.main:app --reload --port 8000
```

Open `http://127.0.0.1:8000/docs` for Swagger UI.

## Endpoints

- `GET /health`
- `POST /v1/predict-fares`

Example request:

```json
{
  "trip_miles": 8.4,
  "trip_minutes": 27.0,
  "pickup_hour": 18,
  "day_of_week": 4,
  "pickup_zone_id": 132,
  "dropoff_zone_id": 230
}
```

The response returns provider-specific historical fare predictions plus an uncertainty band based on each model's holdout MAE.

## Testing

```bash
pytest -q
```
