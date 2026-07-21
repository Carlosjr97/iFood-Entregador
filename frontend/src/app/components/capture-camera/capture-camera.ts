import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';

import { CameraService } from '../../core/services/camera.service';
import { FrameAnalysis } from '../../core/models/frame-analysis.model';

@Component({
  selector: 'app-capture-camera',
  standalone: true,
  templateUrl: './capture-camera.html',
  styleUrl: './capture-camera.scss',
})
export class CaptureCamera implements OnInit, OnChanges, OnDestroy {
  @Input() analysis: FrameAnalysis | null = null;
  @Input() captureIntervalMs = 600;
  @Output() frameCaptured = new EventEmitter<string>();
  @Output() cameraError = new EventEmitter<string>();

  @ViewChild('video', { static: true }) videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('overlay', { static: true }) overlayRef!: ElementRef<HTMLCanvasElement>;

  private readonly cameraService = inject(CameraService);
  private intervalHandle: ReturnType<typeof setInterval> | null = null;

  async ngOnInit(): Promise<void> {
    try {
      await this.cameraService.start(this.videoRef.nativeElement);
      this.intervalHandle = setInterval(() => this.captureFrame(), this.captureIntervalMs);
    } catch (error) {
      console.error('Falha ao iniciar a câmera:', error);
      const message = this.describeError(error);
      this.cameraError.emit(message);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['analysis']) {
      this.drawOverlay();
    }
  }

  ngOnDestroy(): void {
    if (this.intervalHandle) {
      clearInterval(this.intervalHandle);
    }
    this.cameraService.stop();
  }

  private captureFrame(): void {
    const frame = this.cameraService.captureFrameBase64(this.videoRef.nativeElement);
    if (frame) {
      this.frameCaptured.emit(frame);
    }
  }

  private drawOverlay(): void {
    const video = this.videoRef.nativeElement;
    const canvas = this.overlayRef.nativeElement;
    if (!video.videoWidth) {
      return;
    }
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const box = this.analysis?.boundingBox;
    if (box) {
      ctx.strokeStyle = this.boxColor();
      ctx.lineWidth = 4;
      ctx.strokeRect(box.x, box.y, box.width, box.height);
    }
  }

  private boxColor(): string {
    const score = this.analysis?.score ?? 0;
    if (score >= 75) return '#2ecc71';
    if (score >= 45) return '#f1c40f';
    return '#e74c3c';
  }

  private describeError(error: unknown): string {
    const name = error instanceof DOMException ? error.name : null;
    switch (name) {
      case 'NotAllowedError':
        return 'Permissão da câmera negada. Autorize o acesso à câmera nas configurações do navegador e recarregue a página.';
      case 'NotFoundError':
        return 'Nenhuma câmera foi encontrada neste dispositivo.';
      case 'NotReadableError':
        return 'A câmera já está sendo usada por outro aplicativo ou aba.';
      case 'OverconstrainedError':
        return 'A câmera não suporta a resolução solicitada.';
      default:
        return error instanceof Error
          ? error.message
          : 'Não foi possível acessar a câmera. Verifique as permissões do navegador.';
    }
  }
}
