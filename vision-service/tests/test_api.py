from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_endpoint_returns_ok():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_analyze_frame_with_blank_image_returns_no_face(blank_image_b64):
    response = client.post("/analyze-frame", json={"image": blank_image_b64})
    assert response.status_code == 200
    body = response.json()
    assert body["faceDetected"] is False
    assert body["score"] == 0
    assert body["warnings"] == ["Nenhum rosto detectado."]


def test_analyze_frame_rejects_undecodable_payload():
    response = client.post("/analyze-frame", json={"image": "####not-valid-base64####"})
    assert response.status_code == 400


def test_analyze_frame_requires_image_field():
    response = client.post("/analyze-frame", json={})
    assert response.status_code == 422


def test_liveness_rejects_empty_frames():
    response = client.post("/liveness", json={"frames": []})
    assert response.status_code == 422


def test_liveness_with_no_detectable_face_is_not_completed(blank_image_b64):
    response = client.post("/liveness", json={"frames": [blank_image_b64, blank_image_b64]})
    assert response.status_code == 200
    body = response.json()
    assert body["completed"] is False
    assert body["framesAnalyzed"] == 2
