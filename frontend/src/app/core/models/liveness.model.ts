export interface LivenessResult {
  leftTurn: boolean;
  rightTurn: boolean;
  centerReturn: boolean;
  completed: boolean;
  framesAnalyzed: number;
  facesDetectedRatio: number;
}
