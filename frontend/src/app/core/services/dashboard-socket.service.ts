import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { DashboardEvent } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardSocketService {
  private socket: WebSocket | null = null;
  private readonly events$ = new Subject<DashboardEvent>();

  connect(): Observable<DashboardEvent> {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${protocol}://${window.location.host}/ws/dashboard`;
    this.socket = new WebSocket(url);
    this.socket.onmessage = (event) => {
      this.events$.next(JSON.parse(event.data) as DashboardEvent);
    };
    return this.events$.asObservable();
  }

  disconnect(): void {
    this.socket?.close();
    this.socket = null;
  }
}
