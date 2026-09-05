from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_health_endpoint_exists():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] in {"ok", "degraded"}
    assert isinstance(body["models_loaded"], list)
