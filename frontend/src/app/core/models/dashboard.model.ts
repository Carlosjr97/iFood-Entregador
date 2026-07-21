export interface EvolutionPoint {
  date: string;
  averageScore: number;
  sessionsCount: number;
}

export interface DashboardStats {
  totalSessions: number;
  averageScore: number;
  averageDurationSeconds: number;
  failuresByCategory: Record<string, number>;
  evolution: EvolutionPoint[];
}

export interface RankingEntry {
  userId: number;
  userName: string;
  averageScore: number;
  sessionsCount: number;
}

export interface DashboardEvent {
  type: string;
  sessionId: number;
  score: number;
  result: string;
}
