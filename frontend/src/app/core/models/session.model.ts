export type SessionResult = 'PENDING' | 'PASSED' | 'FAILED';

export interface Session {
  id: number;
  userId: number;
  userName: string;
  score: number;
  result: SessionResult;
  createdAt: string;
}

export interface CompleteSessionPayload {
  score: number;
  brightness: number;
  blur: number;
  faceSize: number;
  centered: boolean;
  stability: number;
  livenessCompleted: boolean;
}
