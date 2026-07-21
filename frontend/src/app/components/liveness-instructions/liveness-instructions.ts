import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { LivenessResult } from '../../core/models/liveness.model';

type Phase = 'center' | 'left' | 'center2' | 'right' | 'center3';

interface PhaseStep {
  phase: Phase;
  instruction: string;
  durationMs: number;
}

const STEPS: PhaseStep[] = [
  { phase: 'center', instruction: 'Olhe para frente', durationMs: 1500 },
  { phase: 'left', instruction: 'Vire o rosto para a esquerda', durationMs: 1800 },
  { phase: 'center2', instruction: 'Volte ao centro', durationMs: 1500 },
  { phase: 'right', instruction: 'Vire o rosto para a direita', durationMs: 1800 },
  { phase: 'center3', instruction: 'Volte ao centro novamente', durationMs: 1500 },
];

const FRAME_COLLECTION_INTERVAL_MS = 150;

@Component({
  selector: 'app-liveness-instructions',
  standalone: true,
  imports: [MatButtonModule, MatProgressBarModule],
  templateUrl: './liveness-instructions.html',
  styleUrl: './liveness-instructions.scss',
})
export class LivenessInstructions implements OnDestroy {
  @Input({ required: true }) captureFrame!: () => string | null;
  @Input() result: LivenessResult | null = null;
  @Output() verify = new EventEmitter<string[]>();

  readonly steps = STEPS;
  currentStepIndex = -1;
  running = false;
  progress = 0;

  private frames: string[] = [];
  private collectHandle: ReturnType<typeof setInterval> | null = null;
  private stepTimeout: ReturnType<typeof setTimeout> | null = null;

  get currentInstruction(): string {
    if (this.currentStepIndex < 0 || this.currentStepIndex >= this.steps.length) {
      return 'Verificando...';
    }
    return this.steps[this.currentStepIndex].instruction;
  }

  start(): void {
    this.frames = [];
    this.currentStepIndex = 0;
    this.running = true;
    this.runStep();
  }

  ngOnDestroy(): void {
    this.clearTimers();
  }

  private runStep(): void {
    if (this.currentStepIndex >= this.steps.length) {
      this.finish();
      return;
    }
    const step = this.steps[this.currentStepIndex];
    const stepStart = Date.now();

    this.collectHandle = setInterval(() => {
      const frame = this.captureFrame();
      if (frame) {
        this.frames.push(frame);
      }
      this.progress = Math.min(100, ((Date.now() - stepStart) / step.durationMs) * 100);
    }, FRAME_COLLECTION_INTERVAL_MS);

    this.stepTimeout = setTimeout(() => {
      this.clearTimers();
      this.currentStepIndex += 1;
      this.progress = 0;
      this.runStep();
    }, step.durationMs);
  }

  private finish(): void {
    this.clearTimers();
    this.running = false;
    this.verify.emit(this.frames);
  }

  private clearTimers(): void {
    if (this.collectHandle) {
      clearInterval(this.collectHandle);
      this.collectHandle = null;
    }
    if (this.stepTimeout) {
      clearTimeout(this.stepTimeout);
      this.stepTimeout = null;
    }
  }
}
