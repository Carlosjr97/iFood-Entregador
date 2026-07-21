import numpy as np

from app.services import quality_metrics as qm


def test_compute_brightness_dark_image():
    gray = np.zeros((100, 100), dtype=np.uint8)
    assert qm.compute_brightness(gray) == 0.0


def test_compute_brightness_bright_image():
    gray = np.full((100, 100), 255, dtype=np.uint8)
    assert qm.compute_brightness(gray) == 100.0


def test_compute_contrast_flat_image_is_zero():
    gray = np.full((50, 50), 128, dtype=np.uint8)
    assert qm.compute_contrast(gray) == 0.0


def test_classify_distance_thresholds():
    assert qm.classify_distance(0.05) == "too_far"
    assert qm.classify_distance(0.35) == "good"
    assert qm.classify_distance(0.80) == "too_close"


def test_is_centered_true_for_centered_box():
    box = qm.BoundingBox(x=140, y=90, width=40, height=60)
    assert qm.is_centered(box, (240, 320)) is True


def test_is_centered_false_for_offset_box():
    box = qm.BoundingBox(x=0, y=0, width=40, height=60)
    assert qm.is_centered(box, (240, 320)) is False


def test_build_warnings_dark_environment_mentions_escuro():
    warnings = qm.build_warnings(
        face_detected=True,
        multiple_faces=False,
        brightness_pct=20.0,
        sharpness_pct=80.0,
        distance="good",
        centered=True,
    )
    assert any("escuro" in w for w in warnings)


def test_build_warnings_no_face_returns_single_warning():
    warnings = qm.build_warnings(
        face_detected=False,
        multiple_faces=False,
        brightness_pct=50.0,
        sharpness_pct=0.0,
        distance="unknown",
        centered=False,
    )
    assert warnings == ["Nenhum rosto detectado."]


def test_aggregate_score_no_face_is_zero():
    score = qm.aggregate_score(
        face_detected=False,
        multiple_faces=False,
        brightness_pct=0,
        sharpness_pct=0,
        contrast_pct=0,
        distance="unknown",
        centered=False,
    )
    assert score == 0


def test_aggregate_score_ideal_conditions_is_high():
    score = qm.aggregate_score(
        face_detected=True,
        multiple_faces=False,
        brightness_pct=60.0,
        sharpness_pct=90.0,
        contrast_pct=70.0,
        distance="good",
        centered=True,
    )
    assert score >= 85


def test_aggregate_score_multiple_faces_is_penalized():
    good_score = qm.aggregate_score(
        face_detected=True,
        multiple_faces=False,
        brightness_pct=60.0,
        sharpness_pct=90.0,
        contrast_pct=70.0,
        distance="good",
        centered=True,
    )
    penalized_score = qm.aggregate_score(
        face_detected=True,
        multiple_faces=True,
        brightness_pct=60.0,
        sharpness_pct=90.0,
        contrast_pct=70.0,
        distance="good",
        centered=True,
    )
    assert penalized_score < good_score
