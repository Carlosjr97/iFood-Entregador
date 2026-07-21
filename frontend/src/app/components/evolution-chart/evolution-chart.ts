import { Component, Input, computed, signal } from '@angular/core';

import { EvolutionPoint } from '../../core/models/dashboard.model';

interface PlottedPoint extends EvolutionPoint {
  x: number;
  y: number;
}

@Component({
  selector: 'app-evolution-chart',
  standalone: true,
  templateUrl: './evolution-chart.html',
  styleUrl: './evolution-chart.scss',
})
export class EvolutionChart {
  readonly width = 560;
  readonly height = 220;
  readonly padding = { top: 16, right: 16, bottom: 28, left: 32 };
  readonly gridLines = [0, 25, 50, 75, 100];

  private readonly _data = signal<EvolutionPoint[]>([]);
  hoverIndex = signal<number | null>(null);

  @Input() set data(value: EvolutionPoint[] | null) {
    this._data.set(value ?? []);
  }

  readonly viewBox = `0 0 ${this.width} ${this.height}`;

  readonly points = computed<PlottedPoint[]>(() => {
    const data = this._data();
    if (data.length === 0) {
      return [];
    }
    const innerWidth = this.width - this.padding.left - this.padding.right;
    const innerHeight = this.height - this.padding.top - this.padding.bottom;
    const maxScore = 100;
    return data.map((point, index) => {
      const x =
        data.length === 1
          ? this.padding.left + innerWidth / 2
          : this.padding.left + (innerWidth * index) / (data.length - 1);
      const y = this.padding.top + innerHeight * (1 - point.averageScore / maxScore);
      return { ...point, x, y };
    });
  });

  readonly linePath = computed(() => {
    const points = this.points();
    if (points.length === 0) {
      return '';
    }
    return points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
  });

  get hoverPoint(): PlottedPoint | null {
    const index = this.hoverIndex();
    return index === null ? null : (this.points()[index] ?? null);
  }

  gridY(value: number): number {
    const innerHeight = this.height - this.padding.top - this.padding.bottom;
    return this.padding.top + innerHeight * (1 - value / 100);
  }

  onHover(index: number): void {
    this.hoverIndex.set(index);
  }

  clearHover(): void {
    this.hoverIndex.set(null);
  }
}
