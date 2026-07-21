FROM python:3.12-slim

WORKDIR /app

# libgl1/libglib2.0-0/libgles2/libegl1 are required by mediapipe's native Tasks API bindings
# (they dlopen libGL/libGLESv2/libEGL even for CPU-only inference).
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    libgles2 \
    libegl1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Pre-bake the MediaPipe Tasks model bundles so the image works fully offline at runtime.
RUN mkdir -p /app/models \
    && curl -sSL -o /app/models/blaze_face_short_range.tflite \
       https://storage.googleapis.com/mediapipe-models/face_detector/blaze_face_short_range/float16/1/blaze_face_short_range.tflite \
    && curl -sSL -o /app/models/face_landmarker.task \
       https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task

COPY app ./app

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
