# RideWise

RideWise is an Android ride-decision application that combines real routing data with machine-learning-based fare prediction to help users compare ride options and decide whether to ride now, wait, or walk to a nearby pickup point.

The current version replaces the project's original simulated pricing logic with a deployed FastAPI backend, Google Routes integration, and provider-specific XGBoost models trained on public NYC TLC High Volume For-Hire Vehicle trip data.

## Features

- Compare predicted Uber and Lyft fares
- View traffic-aware trip distance and duration
- See prediction ranges based on historical model error
- Wait & Save recommendations using historical time-of-day patterns
- Walk Nearby recommendations using:
  - Google walking routes
  - alternate pickup points
  - traffic-aware driving routes
  - ML fare predictions
- Open Uber and Lyft using deep links
- Save ride comparison history with Firebase
- Google Places location search
- Cloud-hosted prediction backend

## Tech Stack

### Android
- Java
- Android SDK
- Retrofit
- Gson
- Firebase Authentication
- Cloud Firestore
- Google Places SDK for Android
- Google Maps
- Uber / Lyft deep links

### Backend
- Python 3.13
- FastAPI
- Pydantic
- Uvicorn
- HTTPX

### Machine Learning
- XGBoost
- scikit-learn
- pandas
- joblib
- GeoPandas
- Shapely

### Cloud & APIs
- Google Cloud Run
- Google Routes API
- Google Places API
- NYC TLC public trip data

## Architecture

```text
Android App
    |
    | HTTPS / Retrofit
    v
FastAPI Backend on Google Cloud Run
    |
    +--> Google Routes API
    |       |
    |       +--> driving distance
    |       +--> traffic-aware duration
    |       +--> walking routes
    |
    +--> NYC Taxi Zone Resolver
    |       |
    |       +--> pickup zone
    |       +--> dropoff zone
    |
    +--> XGBoost Fare Models
            |
            +--> Uber prediction
            +--> Lyft prediction
            +--> prediction ranges
```

## Machine Learning Pipeline

RideWise uses public NYC TLC High Volume For-Hire Vehicle trip records.

The training pipeline:

1. Downloads NYC TLC HVFHV trip data
2. Filters and cleans trip records
3. Separates Uber and Lyft trips
4. Engineers route and time features
5. Performs chronological train/validation splitting
6. Trains provider-specific XGBoost regression models
7. Evaluates models using:
   - MAE
   - RMSE
   - R²
   - median absolute error
   - 80th percentile absolute error
8. Saves trained models and evaluation metrics as deployment artifacts

## Current Validation Results

| Provider | MAE | RMSE | R² | Median Error | 80% Error |
|---|---:|---:|---:|---:|---:|
| Uber | ~$4.90 | ~$8.95 | ~0.835 | ~$2.54 | ~$7.10 |
| Lyft | ~$3.15 | ~$5.83 | ~0.894 | ~$1.77 | ~$4.22 |

These models estimate historical fare behavior. RideWise does not claim to provide live Uber or Lyft prices.

## Core API Endpoints

### Health

```http
GET /health
```

### Analyze Trip

```http
POST /v1/analyze-trip
```

Returns:

- traffic-aware route information
- Uber historical fare prediction
- Lyft historical fare prediction
- prediction ranges

### Wait & Save

```http
POST /v1/wait-and-save
```

Evaluates future time windows and recommends whether meaningful predicted savings justify waiting.

### Walk Nearby

```http
POST /v1/walk-nearby
```

Evaluates nearby alternate pickup points using walking routes, driving routes, and ML fare predictions.

## Example Request

```json
{
  "pickup_lat": 40.7580,
  "pickup_lon": -73.9855,
  "dropoff_lat": 40.6413,
  "dropoff_lon": -73.7781
}
```

Example route:

```text
Times Square -> JFK Airport
```

## Local Backend Setup

From the repository root:

```bash
cd backend
```

Create a Python environment:

```bash
python3.13 -m venv .venv
source .venv/bin/activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

Create:

```text
backend/.env
```

with:

```env
GOOGLE_ROUTES_API_KEY=YOUR_SERVER_SIDE_GOOGLE_ROUTES_KEY
```

Download NYC taxi-zone geometry:

```bash
python scripts/download_taxi_zones.py
```

Run the API:

```bash
uvicorn app.main:app --reload
```

The local API will be available at:

```text
http://127.0.0.1:8000
```

## Android Setup

Create or update the root:

```text
local.properties
```

Example:

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=YOUR_ANDROID_MAPS_PLACES_KEY
BACKEND_BASE_URL=https://your-backend-url/
```

Do not commit real API keys.

Build using Java 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew assembleDebug
```

Install on an emulator/device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Model Artifacts

The repository includes trained Uber and Lyft model artifacts so the backend can run without retraining the full dataset.

Raw TLC datasets are intentionally excluded because they are very large.

To retrain the models, use the scripts under:

```text
backend/ml/
```

## Taxi Zone Data

NYC taxi-zone shapefiles are not stored directly in the repository.

They can be downloaded automatically using:

```bash
python backend/scripts/download_taxi_zones.py
```

## Deployment

The FastAPI backend is containerized with Docker and deployed to Google Cloud Run.

The Docker build:

- installs backend dependencies
- copies trained ML artifacts
- downloads NYC taxi-zone geometry
- starts FastAPI using Uvicorn

## Important Limitations

RideWise does not use live Uber or Lyft pricing APIs.

Predictions are based on historical NYC TLC trip patterns and may differ from the final fare shown in the provider application.

Wait & Save recommendations use historical time features rather than guaranteed future price changes.

Walk Nearby recommendations compare alternate pickup locations using real route information and historical ML predictions.

Current model calibration is focused on the New York City market.

## Project Structure

```text
RideWise/
├── app/
│   └── src/main/java/com/example/ridewise/
│       ├── network/
│       ├── repository/
│       ├── models/
│       └── utils/
│
├── backend/
│   ├── app/
│   │   ├── main.py
│   │   ├── schemas.py
│   │   └── services/
│   │
│   ├── ml/
│   │   ├── train.py
│   │   └── artifacts/
│   │
│   ├── scripts/
│   │   └── download_taxi_zones.py
│   │
│   ├── Dockerfile
│   └── requirements.txt
│
└── README.md
```

## Status

Core functionality is complete:

- Android-to-backend integration
- provider-specific ML predictions
- Google Routes integration
- NYC geospatial zone resolution
- Wait & Save
- Walk Nearby
- Cloud Run deployment
- legacy simulated pricing removal

Remaining work is focused on final polish, screenshots, and documentation.

## Disclaimer

RideWise is an independent educational project and is not affiliated with, endorsed by, or sponsored by Uber, Lyft, Google, or the NYC Taxi and Limousine Commission.