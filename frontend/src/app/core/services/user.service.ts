import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

import { User } from '../models/user.model';

const STORAGE_KEY = 'biometric.currentUser';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  createOrGetUser(name: string, email: string): Observable<User> {
    return this.http.post<User>('/api/users', { name, email });
  }

  listUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }

  /**
   * There is no login flow — the capture screen needs a userId to open a session but must not
   * ask the person to fill anything in. This silently provisions (and caches) a per-device
   * identity on first use, and reuses it on every later visit.
   */
  getOrCreateDeviceUser(): Observable<User> {
    const stored = this.getStoredUser();
    if (stored) {
      return of(stored);
    }
    const deviceId = crypto.randomUUID();
    return this.createOrGetUser('Usuário do dispositivo', `dispositivo-${deviceId}@app.local`).pipe(
      tap((user) => this.storeUser(user)),
    );
  }

  getStoredUser(): User | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  }

  storeUser(user: User): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  }

  clearStoredUser(): void {
    localStorage.removeItem(STORAGE_KEY);
  }
}
