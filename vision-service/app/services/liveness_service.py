"""Prova de vida: detecta o gesto centro -> lateral -> centro -> lateral oposta -> centro
usando o ângulo de yaw da cabeça, estimado por PnP a partir de 6 landmarks canônicos do
MediaPipe Face Landmarker (nariz, queixo, cantos dos olhos e da boca) contra um modelo 3D
genérico de rosto. Isso mede geometria real do rosto no frame — não há atalho para "aprovar"
sem que o gesto de fato ocorra na sequência de imagens recebida.
"""
import math
from dataclasses import dataclass

import cv2
import mediapipe as mp
import numpy as np
from mediapipe.tasks.python.core.base_options import BaseOptions
from mediapipe.tasks.python.vision import face_landmarker as mp_face_landmarker

from app.core.models import ensure_face_landmarker_model
from app.utils.image_decode import decode_base64_image

_landmarker: mp_face_landmarker.FaceLandmarker | None = None


def _get_landmarker() -> mp_face_landmarker.FaceLandmarker:
    global _landmarker
    if _landmarker is None:
        options = mp_face_landmarker.FaceLandmarkerOptions(
            base_options=BaseOptions(model_asset_path=ensure_face_landmarker_model()),
            num_faces=1,
            min_face_detection_confidence=0.5,
        )
        _landmarker = mp_face_landmarker.FaceLandmarker.create_from_options(options)
    return _landmarker


_MODEL_POINTS = np.array(
    [
        (0.0, 0.0, 0.0),  # nose tip
        (0.0, -330.0, -65.0),  # chin
        (-225.0, 170.0, -135.0),  # left eye, left corner
        (225.0, 170.0, -135.0),  # right eye, right corner
        (-150.0, -150.0, -125.0),  # left mouth corner
        (150.0, -150.0, -125.0),  # right mouth corner
    ],
    dtype=np.float64,
)

_LANDMARK_IDS = {
    "nose_tip": 1,
    "chin": 152,
    "left_eye_left_corner": 33,
    "right_eye_right_corner": 263,
    "left_mouth_corner": 61,
    "right_mouth_corner": 291,
}

YAW_LEFT_THRESHOLD = -15.0
YAW_RIGHT_THRESHOLD = 15.0
YAW_CENTER_BAND = 10.0


def estimate_yaw(landmarks, image_shape: tuple[int, int]) -> float | None:
    h, w = image_shape
    try:
        image_points = np.array(
            [
                (landmarks[idx].x * w, landmarks[idx].y * h)
                for idx in (
                    _LANDMARK_IDS["nose_tip"],
                    _LANDMARK_IDS["chin"],
                    _LANDMARK_IDS["left_eye_left_corner"],
                    _LANDMARK_IDS["right_eye_right_corner"],
                    _LANDMARK_IDS["left_mouth_corner"],
                    _LANDMARK_IDS["right_mouth_corner"],
                )
            ],
            dtype=np.float64,
        )
    except IndexError:
        return None

    focal_length = w
    center = (w / 2.0, h / 2.0)
    camera_matrix = np.array(
        [
            [focal_length, 0, center[0]],
            [0, focal_length, center[1]],
            [0, 0, 1],
        ],
        dtype=np.float64,
    )
    dist_coeffs = np.zeros((4, 1))

    success, rotation_vector, _ = cv2.solvePnP(
        _MODEL_POINTS,
        image_points,
        camera_matrix,
        dist_coeffs,
        flags=cv2.SOLVEPNP_ITERATIVE,
    )
    if not success:
        return None

    rotation_matrix, _ = cv2.Rodrigues(rotation_vector)
    sy = math.sqrt(rotation_matrix[0, 0] ** 2 + rotation_matrix[1, 0] ** 2)
    if sy >= 1e-6:
        yaw = math.atan2(-rotation_matrix[2, 0], sy)
    else:
        yaw = math.atan2(-rotation_matrix[1, 2], rotation_matrix[1, 1])
    return math.degrees(yaw)


def classify_yaw(yaw: float) -> str:
    if yaw <= YAW_LEFT_THRESHOLD:
        return "left"
    if yaw >= YAW_RIGHT_THRESHOLD:
        return "right"
    if -YAW_CENTER_BAND <= yaw <= YAW_CENTER_BAND:
        return "center"
    return "transition"


def _collapse(sequence: list[str]) -> list[str]:
    collapsed: list[str] = []
    for item in sequence:
        if item == "transition":
            continue
        if not collapsed or collapsed[-1] != item:
            collapsed.append(item)
    return collapsed


def _has_subsequence(collapsed: list[str], pattern: list[str]) -> bool:
    it = iter(collapsed)
    return all(any(x == token for x in it) for token in pattern)


@dataclass
class LivenessResult:
    left_turn: bool
    right_turn: bool
    center_return: bool
    completed: bool
    frames_analyzed: int
    faces_detected_ratio: float


def verify_sequence(frames_b64: list[str]) -> LivenessResult:
    classifications: list[str] = []
    detected_count = 0
    landmarker = _get_landmarker()

    for frame_b64 in frames_b64:
        try:
            image = decode_base64_image(frame_b64)
        except ValueError:
            continue
        rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)
        result = landmarker.detect(mp_image)
        if not result.face_landmarks:
            continue
        detected_count += 1
        landmarks = result.face_landmarks[0]
        yaw = estimate_yaw(landmarks, image.shape[:2])
        if yaw is None:
            continue
        classifications.append(classify_yaw(yaw))

    collapsed = _collapse(classifications)
    left_turn = "left" in collapsed
    right_turn = "right" in collapsed
    center_return = collapsed.count("center") >= 2 if (left_turn or right_turn) else False
    completed = _has_subsequence(
        collapsed, ["center", "left", "center", "right", "center"]
    ) or _has_subsequence(collapsed, ["center", "right", "center", "left", "center"])

    total = len(frames_b64) or 1
    return LivenessResult(
        left_turn=left_turn,
        right_turn=right_turn,
        center_return=center_return,
        completed=completed,
        frames_analyzed=len(frames_b64),
        faces_detected_ratio=round(detected_count / total, 3),
    )
