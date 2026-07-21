from pydantic import BaseModel, Field


class LivenessRequest(BaseModel):
    frames: list[str] = Field(
        ...,
        min_length=1,
        description="Sequência ordenada de frames em base64 cobrindo o gesto de prova de vida.",
    )
    sessionId: str | None = None


class LivenessResponse(BaseModel):
    leftTurn: bool
    rightTurn: bool
    centerReturn: bool
    completed: bool
    framesAnalyzed: int
    facesDetectedRatio: float
