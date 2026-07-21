export interface BoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

export type DistanceStatus = 'too_far' | 'good' | 'too_close' | 'unknown';

export interface FrameAnalysis {
  faceDetected: boolean;
  brightness: number;
  blur: number;
  contrast: number;
  centered: boolean;
  distance: DistanceStatus;
  faceSize: number;
  boundingBox: BoundingBox | null;
  score: number;
  warnings: string[];
}

export interface CaptureFrameResult {
  type: 'frame_result';
  analysis: FrameAnalysis;
  stability: number;
}

export interface CaptureErrorMessage {
  type: 'error';
  message: string;
}

export type CaptureSocketMessage = CaptureFrameResult | CaptureErrorMessage;
