"""Pure, testable image-quality heuristics used by face_analysis.

All thresholds here are demo-grade heuristics (not a calibrated ML model) chosen to give
sensible, explainable feedback (brightness/sharpness/framing) without any third-party
face-quality dataset.
"""
from dataclasses import dataclass

import cv2
import numpy as np

FACE_SIZE_TOO_FAR = 0.18
FACE_SIZE_TOO_CLOSE = 0.65
CENTER_TOLERANCE = 0.15
BRIGHTNESS_LOW = 40.0
BRIGHTNESS_HIGH = 85.0
BLUR_LOW = 45.0


@dataclass
class BoundingBox:
    x: int
    y: int
    width: int
    height: int


def compute_brightness(gray: np.ndarray) -> float:
    """Mean pixel intensity mapped to a 0-100 scale."""
    return round(float(np.mean(gray)) / 255.0 * 100.0, 1)


def compute_contrast(gray: np.ndarray) -> float:
    """Standard deviation of pixel intensity mapped to a 0-100 scale."""
    std = float(np.std(gray))
    return round(min(100.0, std / 80.0 * 100.0), 1)


def compute_sharpness(gray: np.ndarray) -> float:
    """Variance of the Laplacian (focus measure) mapped to a 0-100 scale. Higher = sharper."""
    variance = cv2.Laplacian(gray, cv2.CV_64F).var()
    return round(min(100.0, float(variance) / 800.0 * 100.0), 1)


def crop_face(gray: np.ndarray, box: BoundingBox, margin: float = 0.25) -> np.ndarray:
    h, w = gray.shape[:2]
    mx = int(box.width * margin)
    my = int(box.height * margin)
    x1 = max(box.x - mx, 0)
    y1 = max(box.y - my, 0)
    x2 = min(box.x + box.width + mx, w)
    y2 = min(box.y + box.height + my, h)
    if x2 - x1 < 20 or y2 - y1 < 20:
        return gray
    return gray[y1:y2, x1:x2]


def classify_distance(face_height_ratio: float) -> str:
    if face_height_ratio < FACE_SIZE_TOO_FAR:
        return "too_far"
    if face_height_ratio > FACE_SIZE_TOO_CLOSE:
        return "too_close"
    return "good"


def is_centered(box: BoundingBox, image_shape: tuple[int, int]) -> bool:
    h, w = image_shape
    cx = (box.x + box.width / 2) / w
    cy = (box.y + box.height / 2) / h
    return abs(cx - 0.5) <= CENTER_TOLERANCE and abs(cy - 0.5) <= CENTER_TOLERANCE


def brightness_goodness(brightness_pct: float) -> float:
    if BRIGHTNESS_LOW <= brightness_pct <= BRIGHTNESS_HIGH:
        return 100.0
    if brightness_pct < BRIGHTNESS_LOW:
        return max(0.0, brightness_pct / BRIGHTNESS_LOW * 100.0)
    return max(0.0, (100.0 - brightness_pct) / (100.0 - BRIGHTNESS_HIGH) * 100.0)


def contrast_goodness(contrast_pct: float) -> float:
    return 100.0 if contrast_pct >= 45.0 else max(0.0, contrast_pct / 45.0 * 100.0)


def build_warnings(
    *,
    face_detected: bool,
    multiple_faces: bool,
    brightness_pct: float,
    sharpness_pct: float,
    distance: str,
    centered: bool,
) -> list[str]:
    warnings: list[str] = []
    if not face_detected:
        warnings.append("Nenhum rosto detectado.")
        return warnings

    if multiple_faces:
        warnings.append("Mais de um rosto detectado. Garanta que apenas uma pessoa apareça no quadro.")
    if brightness_pct < BRIGHTNESS_LOW:
        warnings.append("Ambiente muito escuro. Procure um local mais iluminado.")
    elif brightness_pct > BRIGHTNESS_HIGH:
        warnings.append("Ambiente muito claro. Reduza a luz direta sobre o rosto.")
    if sharpness_pct < BLUR_LOW:
        warnings.append("Imagem borrada. Mantenha a câmera firme.")
    if distance == "too_far":
        warnings.append("Aproxime o rosto da câmera.")
    elif distance == "too_close":
        warnings.append("Afaste um pouco o rosto da câmera.")
    if not centered:
        warnings.append("Centralize o rosto no quadro.")
    return warnings


def aggregate_score(
    *,
    face_detected: bool,
    multiple_faces: bool,
    brightness_pct: float,
    sharpness_pct: float,
    contrast_pct: float,
    distance: str,
    centered: bool,
) -> int:
    if not face_detected:
        return 0

    distance_score = 100.0 if distance == "good" else 40.0
    centered_score = 100.0 if centered else 40.0
    weighted = (
        brightness_goodness(brightness_pct) * 0.25
        + sharpness_pct * 0.30
        + distance_score * 0.20
        + centered_score * 0.15
        + contrast_goodness(contrast_pct) * 0.10
    )
    if multiple_faces:
        weighted *= 0.4
    return int(round(max(0.0, min(100.0, weighted))))
