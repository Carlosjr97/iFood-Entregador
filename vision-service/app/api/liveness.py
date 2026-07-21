import logging

from fastapi import APIRouter, HTTPException

from app.schemas.liveness import LivenessRequest, LivenessResponse
from app.services.liveness_service import verify_sequence

router = APIRouter(tags=["liveness"])
logger = logging.getLogger("vision.liveness")


@router.post("/liveness", response_model=LivenessResponse)
def liveness_endpoint(payload: LivenessRequest) -> LivenessResponse:
    if not payload.frames:
        raise HTTPException(status_code=400, detail="Nenhum frame enviado.")

    result = verify_sequence(payload.frames)
    logger.info(
        "liveness_verified",
        extra={"extra_fields": {"completed": result.completed, "sessionId": payload.sessionId}},
    )
    return LivenessResponse(
        leftTurn=result.left_turn,
        rightTurn=result.right_turn,
        centerReturn=result.center_return,
        completed=result.completed,
        framesAnalyzed=result.frames_analyzed,
        facesDetectedRatio=result.faces_detected_ratio,
    )
