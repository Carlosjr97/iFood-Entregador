import base64

import cv2
import numpy as np
import pytest


def _encode_jpeg(image: np.ndarray) -> str:
    ok, buffer = cv2.imencode(".jpg", image)
    assert ok
    return base64.b64encode(buffer.tobytes()).decode("utf-8")


@pytest.fixture
def blank_image_b64() -> str:
    image = np.zeros((240, 320, 3), dtype=np.uint8)
    return _encode_jpeg(image)
