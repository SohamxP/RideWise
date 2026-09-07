from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_prediction_rejects_invalid_trip_distance():
    response = client.post(
        "/v1/predict-fares",
        json={
            "trip_miles": -1,
            "trip_minutes": 20,
            "pickup_hour": 14,
            "day_of_week": 2,
            "pickup_zone_id": 132,
            "dropoff_zone_id": 138,
        },
    )
    assert response.status_code == 422
