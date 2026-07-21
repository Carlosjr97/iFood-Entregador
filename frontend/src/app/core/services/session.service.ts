import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { LivenessResult } from '../models/liveness.model';
import { CompleteSessionPayload, Session } from '../models/session.model';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly http = inject(HttpClient);

  createSession(userId: number): Observable<Session> {
    return this.http.post<Session>('/api/sessions', { userId });
  }

  completeSession(sessionId: number, payload: CompleteSessionPayload): Observable<Session> {
    return this.http.post<Session>(`/api/sessions/${sessionId}/complete`, payload);
  }

  getSession(sessionId: number): Observable<Session> {
    return this.http.get<Session>(`/api/sessions/${sessionId}`);
  }

  listSessions(userId?: number): Observable<Session[]> {
    return this.http.get<Session[]>('/api/sessions', {
      params: userId ? { userId: String(userId) } : {},
    });
  }

  verifyLiveness(sessionId: number, frames: string[]): Observable<LivenessResult> {
    return this.http.post<LivenessResult>('/api/liveness/verify', { sessionId, frames });
  }

  exportCsvUrl(): string {
    return '/api/sessions/export';
  }
}
