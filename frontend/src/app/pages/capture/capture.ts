import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { CaptureCamera } from '../../components/capture-camera/capture-camera';
import { QualityIndicators } from '../../components/quality-indicators/quality-indicators';
import { FrameAnalysis } from '../../core/models/frame-analysis.model';
import { Session } from '../../core/models/session.model';
import { AccountRestrictionsService } from '../../core/services/account-restrictions.service';
import { CaptureSocketService } from '../../core/services/capture-socket.service';
import { SessionService } from '../../core/services/session.service';
import { UserService } from '../../core/services/user.service';

type FlowStep = 'loading' | 'capture' | 'result';

@Component({
  selector: 'app-capture-page',
  standalone: true,
  imports: [MatButtonModule, MatCardModule, MatProgressSpinnerModule, CaptureCamera, QualityIndicators],
  templateUrl: './capture.html',
  styleUrl: './capture.scss',
})
export class CapturePage implements OnInit, OnDestroy {
  private readonly userService = inject(UserService);
  private readonly sessionService = inject(SessionService);
  private readonly captureSocket = inject(CaptureSocketService);
  private readonly accountRestrictions = inject(AccountRestrictionsService);
  private readonly router = inject(Router);

  step = signal<FlowStep>('loading');
  session = signal<Session | null>(null);
  analysis = signal<FrameAnalysis | null>(null);
  stability = signal(0);
  finalResult = signal<Session | null>(null);
  errorMessage = signal<string | null>(null);

  private socketSubscription?: Subscription;
  private completingSession = false;

  ngOnInit(): void {
    // No login screen exists — provision a device identity silently and go straight to the
    // camera, instead of asking the person to fill in a name/e-mail form first.
    this.userService.getOrCreateDeviceUser().subscribe({
      next: (user) => this.startSession(user.id),
      error: () => this.errorMessage.set('Não foi possível preparar a captura.'),
    });
  }

  ngOnDestroy(): void {
    this.socketSubscription?.unsubscribe();
    this.captureSocket.disconnect();
  }

  onFrameCaptured(frameBase64: string): void {
    this.captureSocket.sendFrame(frameBase64);
  }

  goToHome(): void {
    this.router.navigateByUrl('/');
  }

  restart(): void {
    this.captureSocket.disconnect();
    this.session.set(null);
    this.analysis.set(null);
    this.stability.set(0);
    this.finalResult.set(null);
    this.completingSession = false;
    this.step.set('loading');
    this.ngOnInit();
  }

  private startSession(userId: number): void {
    this.sessionService.createSession(userId).subscribe({
      next: (session) => {
        this.session.set(session);
        this.step.set('capture');
        this.socketSubscription = this.captureSocket.connect(session.id).subscribe((message) => {
          if (message.type === 'frame_result') {
            this.analysis.set(message.analysis);
            this.stability.set(message.stability);
            this.maybeCompleteOnIdealQuality(message.analysis, message.stability);
          } else {
            this.errorMessage.set(message.message);
          }
        });
      },
      error: () => this.errorMessage.set('Não foi possível iniciar a sessão.'),
    });
  }

  /** As soon as the frame quality is ideal (no warnings), facial recognition is considered
   * successful — there is no liveness/proof-of-life step in this flow. */
  private maybeCompleteOnIdealQuality(analysis: FrameAnalysis, stability: number): void {
    if (this.completingSession) {
      return;
    }
    const isIdeal = analysis.faceDetected && analysis.warnings.length === 0;
    if (!isIdeal) {
      return;
    }
    this.completingSession = true;
    this.completeSession(analysis, stability);
  }

  private completeSession(analysis: FrameAnalysis, stability: number): void {
    const session = this.session();
    if (!session) {
      return;
    }
    this.sessionService
      .completeSession(session.id, {
        score: analysis.score,
        brightness: analysis.brightness,
        blur: analysis.blur,
        faceSize: analysis.faceSize,
        centered: analysis.centered,
        stability,
        livenessCompleted: true,
      })
      .subscribe({
        next: (updated) => {
          this.captureSocket.disconnect();
          if (updated.result === 'PASSED') {
            // Success goes straight back to the "Disponível" home screen — no intermediate
            // result card asking the person whether they want to return.
            this.accountRestrictions.resolveFacialRecognition();
            this.goToHome();
            return;
          }
          this.finalResult.set(updated);
          this.step.set('result');
        },
        error: () => {
          this.errorMessage.set('Não foi possível concluir a sessão.');
          this.completingSession = false;
        },
      });
  }
}
