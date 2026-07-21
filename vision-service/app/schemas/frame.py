from pydantic import BaseModel, Field


class FrameAnalysisRequest(BaseModel):
    image: str = Field(
        ...,
        description="Imagem JPEG/PNG codificada em base64 (com ou sem prefixo data URL).",
        min_length=1,
    )


class BoundingBoxModel(BaseModel):
    x: int
    y: int
    width: int
    height: int


class FrameAnalysisResponse(BaseModel):
    faceDetected: bool
    brightness: float
    blur: float
    contrast: float
    centered: bool
    distance: str
    faceSize: float
    boundingBox: BoundingBoxModel | None = None
    score: int
    warnings: list[str]
