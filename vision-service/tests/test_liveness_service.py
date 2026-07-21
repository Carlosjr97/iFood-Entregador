from app.services.liveness_service import (
    _collapse,
    _has_subsequence,
    classify_yaw,
    verify_sequence,
)


def test_classify_yaw_center():
    assert classify_yaw(0.0) == "center"
    assert classify_yaw(5.0) == "center"


def test_classify_yaw_left_and_right():
    assert classify_yaw(-20.0) == "left"
    assert classify_yaw(20.0) == "right"


def test_classify_yaw_transition_zone_between_bands():
    assert classify_yaw(12.0) == "transition"
    assert classify_yaw(-12.0) == "transition"


def test_collapse_removes_transitions_and_consecutive_repeats():
    sequence = ["center", "center", "transition", "left", "left", "transition", "center"]
    assert _collapse(sequence) == ["center", "left", "center"]


def test_has_subsequence_true_when_pattern_present_in_order():
    collapsed = ["center", "left", "center", "right", "center"]
    assert _has_subsequence(collapsed, ["center", "left", "center", "right", "center"])


def test_has_subsequence_false_when_step_missing():
    collapsed = ["center", "left", "right"]
    assert not _has_subsequence(collapsed, ["center", "left", "center", "right", "center"])


def test_verify_sequence_empty_frames_is_not_completed():
    result = verify_sequence([])
    assert result.completed is False
    assert result.left_turn is False
    assert result.right_turn is False
    assert result.frames_analyzed == 0
    assert result.faces_detected_ratio == 0.0


def test_verify_sequence_invalid_frames_are_skipped_not_fatal():
    result = verify_sequence(["not-a-real-frame", "also-invalid"])
    assert result.completed is False
    assert result.frames_analyzed == 2
    assert result.faces_detected_ratio == 0.0
