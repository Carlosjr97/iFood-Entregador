import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { CaptureSocketMessage } from '../models/frame-analysis.model';

const RECONNECT_DELAY_MS = 500;

@Injectable({ providedIn: 'root' })
export class CaptureSocketService {
  private socket: WebSocket | null = null;
  private readonly messages$ = new Subject<CaptureSocketMessage>();
  private intentionalClose = false;

  connect(sessionId: number): Observable<CaptureSocketMessage> {
    this.openSocket(sessionId);
    return this.messages$.asObservable();
  }

  sendFrame(imageBase64: string): void {
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify({ type: 'frame', image: imageBase64 }));
    }
  }

  disconnect(): void {
    this.intentionalClose = true;
    this.socket?.close();
    this.socket = null;
  }

  private openSocket(sessionId: number): void {
    this.intentionalClose = false;
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${protocol}://${window.location.host}/ws/capture?sessionId=${sessionId}`;
    this.socket = new WebSocket(url);

    this.socket.onmessage = (event) => {
      this.messages$.next(JSON.parse(event.data) as CaptureSocketMessage);
    };

    this.socket.onerror = () => {
      this.messages$.next({ type: 'error', message: 'Falha na conexão em tempo real com o servidor.' });
    };

    // A closed-but-not-intentional connection (e.g. a transient network blip, or the server
    // dropping the socket) would otherwise leave the UI silently stuck showing the last result
    // forever with no indication anything went wrong — so surface it and retry.
    this.socket.onclose = (event) => {
      if (this.intentionalClose) {
        return;
      }
      console.error('Conexão de captura encerrada inesperadamente:', event.code, event.reason);
      this.messages$.next({
        type: 'error',
        message: `Conexão em tempo real encerrada (código ${event.code}). Tentando reconectar...`,
      });
      setTimeout(() => {
        if (!this.intentionalClose) {
          this.openSocket(sessionId);
        }
      }, RECONNECT_DELAY_MS);
    };
  }
}
