import base64
import re

import cv2
import numpy as np

_DATA_URL_RE = re.compile(r"^data:image/\w+;base64,")


def decode_base64_image(data: str) -> np.ndarray:
    """Decodes a base64 (optionally data-URL prefixed) image into a BGR numpy array."""
    if not data or not data.strip():
        raise ValueError("Imagem vazia.")

    cleaned = _DATA_URL_RE.sub("", data.strip())
    try:
        raw = base64.b64decode(cleaned, validate=False)
    except Exception as exc:  # noqa: BLE001 - any decode failure maps to a 400
        raise ValueError("Base64 inválido.") from exc

    if not raw:
        raise ValueError("Base64 inválido.")

    buffer = np.frombuffer(raw, dtype=np.uint8)
    image = cv2.imdecode(buffer, cv2.IMREAD_COLOR)
    if image is None:
        raise ValueError("Não foi possível decodificar a imagem.")
    return image
