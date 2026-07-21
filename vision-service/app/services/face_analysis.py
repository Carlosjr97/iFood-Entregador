import cv2
import mediapipe as mp
from mediapipe.tasks.python.core.base_options import BaseOptions
from mediapipe.tasks.python.vision import face_detector as mp_face_detector

from app.core.models import ensure_face_detector_model
from app.schemas.frame import BoundingBoxModel, FrameAnalysisResponse
from app.services import quality_metrics as qm
from app.utils.image_decode import decode_base64_image

_detector: mp_face_detector.FaceDetector | None = None


def _get_detector() -> mp_face_detector.FaceDetector:
    global _detector
    if _detector is None:
        options = mp_face_detector.FaceDetectorOptions(
            base_options=BaseOptions(model_asset_path=ensure_face_detector_model()),
            min_detection_confidence=0.5,
        )
        _detector = mp_face_detector.FaceDetector.create_from_options(options)
    return _detector


def _box_area(detection) -> int:
    box = detection.bounding_box
    return max(box.width, 0) * max(box.height, 0)


def analyze_frame(image_b64: str) -> FrameAnalysisResponse:
    image = decode_base64_image(image_b64)
    rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    gray_full = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    h, w = gray_full.shape[:2]

    mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)
    result = _get_detector().detect(mp_image)

    detections = result.detections
    brightness_pct = qm.compute_brightness(gray_full)
    contrast_pct = qm.compute_contrast(gray_full)

    if not detections:
        warnings = qm.build_warnings(
            face_detected=False,
            multiple_faces=False,
            brightness_pct=brightness_pct,
            sharpness_pct=0.0,
            distance="unknown",
            centered=False,
        )
        return FrameAnalysisResponse(
            faceDetected=False,
            brightness=brightness_pct,
            blur=0.0,
            contrast=contrast_pct,
            centered=False,
            distance="unknown",
            faceSize=0.0,
            boundingBox=None,
            score=0,
            warnings=warnings,
        )

    multiple_faces = len(detections) > 1
    primary = max(detections, key=_box_area)
    raw_box = primary.bounding_box
    box = qm.BoundingBox(
        x=max(raw_box.origin_x, 0),
        y=max(raw_box.origin_y, 0),
        width=max(raw_box.width, 1),
        height=max(raw_box.height, 1),
    )

    face_crop = qm.crop_face(gray_full, box)
    sharpness_pct = qm.compute_sharpness(face_crop)
    face_height_ratio = box.height / h
    distance = qm.classify_distance(face_height_ratio)
    centered = qm.is_centered(box, (h, w))

    warnings = qm.build_warnings(
        face_detected=True,
        multiple_faces=multiple_faces,
        brightness_pct=brightness_pct,
        sharpness_pct=sharpness_pct,
        distance=distance,
        centered=centered,
    )
    score = qm.aggregate_score(
        face_detected=True,
        multiple_faces=multiple_faces,
        brightness_pct=brightness_pct,
        sharpness_pct=sharpness_pct,
        contrast_pct=contrast_pct,
        distance=distance,
        centered=centered,
    )

    return FrameAnalysisResponse(
        faceDetected=True,
        brightness=brightness_pct,
        blur=sharpness_pct,
        contrast=contrast_pct,
        centered=centered,
        distance=distance,
        faceSize=round(face_height_ratio, 3),
        boundingBox=BoundingBoxModel(x=box.x, y=box.y, width=box.width, height=box.height),
        score=score,
        warnings=warnings,
    )
