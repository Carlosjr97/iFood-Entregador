import logging

from fastapi import APIRouter, HTTPException

from app.schemas.frame import FrameAnalysisRequest, FrameAnalysisResponse
from app.services.face_analysis import analyze_frame

router = APIRouter(tags=["analysis"])
logger = logging.getLogger("vision.analyze")


@router.post("/analyze-frame", response_model=FrameAnalysisResponse)
def analyze_frame_endpoint(payload: FrameAnalysisRequest) -> FrameAnalysisResponse:
    try:
        response = analyze_frame(payload.image)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    logger.info(
        "frame_analyzed",
        extra={"extra_fields": {"score": response.score, "faceDetected": response.faceDetected}},
    )
    return response
