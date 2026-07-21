"""Downloads and caches the MediaPipe Tasks model bundles.

The mediapipe wheel available for Python 3.12 (>=0.10.x) ships only the new Tasks API —
the legacy `mp.solutions.face_detection` / `mp.solutions.face_mesh` modules used in older
tutorials are not part of this build. The Tasks API requires the model weights to be
downloaded separately (they are not bundled with the pip package). The Dockerfile
pre-downloads these into /app/models at build time; this module also lazily downloads them
if missing, so `pytest` works locally without a manual setup step.
"""
import logging
import urllib.request
from pathlib import Path

logger = logging.getLogger("vision.models")

MODELS_DIR = Path(__file__).resolve().parent.parent.parent / "models"

FACE_DETECTOR_URL = (
    "https://storage.googleapis.com/mediapipe-models/face_detector/"
    "blaze_face_short_range/float16/1/blaze_face_short_range.tflite"
)
FACE_LANDMARKER_URL = (
    "https://storage.googleapis.com/mediapipe-models/face_landmarker/"
    "face_landmarker/float16/1/face_landmarker.task"
)

FACE_DETECTOR_PATH = MODELS_DIR / "blaze_face_short_range.tflite"
FACE_LANDMARKER_PATH = MODELS_DIR / "face_landmarker.task"


def _ensure_model(path: Path, url: str) -> str:
    if path.exists() and path.stat().st_size > 0:
        return str(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    logger.info("downloading_model", extra={"extra_fields": {"url": url, "path": str(path)}})
    urllib.request.urlretrieve(url, path)  # noqa: S310 - fixed, trusted Google model URLs
    return str(path)


def ensure_face_detector_model() -> str:
    return _ensure_model(FACE_DETECTOR_PATH, FACE_DETECTOR_URL)


def ensure_face_landmarker_model() -> str:
    return _ensure_model(FACE_LANDMARKER_PATH, FACE_LANDMARKER_URL)
