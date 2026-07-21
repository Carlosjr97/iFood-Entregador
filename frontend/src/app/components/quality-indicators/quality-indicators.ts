import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { FrameAnalysis } from '../../core/models/frame-analysis.model';

type IndicatorLevel = 'good' | 'warn' | 'bad';
type QualityStatus = 'waiting' | 'suggestion' | 'success';

interface Indicator {
  label: string;
  value: string;
  level: IndicatorLevel;
}

const SUCCESS_MESSAGE = 'Reconhecimento facial feito com sucesso! A qualidade da captura está aprovada.';
const WAITING_MESSAGE = 'Posicione seu rosto no quadro para iniciar a análise.';

@Component({
  selector: 'app-quality-indicators',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  templateUrl: './quality-indicators.html',
  styleUrl: './quality-indicators.scss',
})
export class QualityIndicators {
  @Input() analysis: FrameAnalysis | null = null;
  @Input() stability: number | null = null;

  get overallLevel(): IndicatorLevel {
    return this.levelForScore(this.analysis?.score ?? 0);
  }

  /** Drives the prominent status banner: keeps giving suggestions until every check clears,
   * then flips to a clear success message — instead of leaving the user to infer readiness
   * from a numeric score. */
  get status(): QualityStatus {
    if (!this.analysis?.faceDetected) {
      return 'waiting';
    }
    return this.analysis.warnings.length > 0 ? 'suggestion' : 'success';
  }

  get statusMessage(): string {
    switch (this.status) {
      case 'waiting':
        return WAITING_MESSAGE;
      case 'success':
        return SUCCESS_MESSAGE;
      case 'suggestion':
        return this.analysis!.warnings[0];
    }
  }

  get statusIcon(): string {
    switch (this.status) {
      case 'success':
        return 'check_circle';
      case 'suggestion':
        return 'error_outline';
      default:
        return 'face';
    }
  }

  get indicators(): Indicator[] {
    const analysis = this.analysis;
    if (!analysis || !analysis.faceDetected) {
      return [];
    }
    return [
      {
        label: 'Brilho',
        value: `${Math.round(analysis.brightness)}%`,
        level: this.levelForRange(analysis.brightness, 40, 85),
      },
      { label: 'Nitidez', value: `${Math.round(analysis.blur)}%`, level: this.levelForScore(analysis.blur) },
      {
        label: 'Distância',
        value: this.distanceLabel(analysis.distance),
        level: analysis.distance === 'good' ? 'good' : 'warn',
      },
      {
        label: 'Enquadramento',
        value: analysis.centered ? 'Centralizado' : 'Descentralizado',
        level: analysis.centered ? 'good' : 'warn',
      },
      {
        label: 'Estabilidade',
        value: `${Math.round(this.stability ?? 0)}%`,
        level: this.levelForScore(this.stability ?? 0),
      },
    ];
  }

  private distanceLabel(distance: FrameAnalysis['distance']): string {
    switch (distance) {
      case 'too_far':
        return 'Muito longe';
      case 'too_close':
        return 'Muito perto';
      case 'good':
        return 'Ideal';
      default:
        return 'Desconhecida';
    }
  }

  private levelForScore(value: number): IndicatorLevel {
    if (value >= 70) return 'good';
    if (value >= 40) return 'warn';
    return 'bad';
  }

  private levelForRange(value: number, low: number, high: number): IndicatorLevel {
    if (value >= low && value <= high) return 'good';
    if (value >= low - 15 && value <= high + 15) return 'warn';
    return 'bad';
  }
}
