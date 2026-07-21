import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

import { DashboardStats } from '../../core/models/dashboard.model';
import { EvolutionChart } from '../evolution-chart/evolution-chart';

const CATEGORY_LABELS: Record<string, string> = {
  brilho: 'Brilho',
  nitidez: 'Nitidez',
  enquadramento: 'Enquadramento',
  distancia: 'Distância',
  estabilidade: 'Estabilidade',
};

@Component({
  selector: 'app-dashboard-metrics',
  standalone: true,
  imports: [MatCardModule, EvolutionChart],
  templateUrl: './dashboard-metrics.html',
  styleUrl: './dashboard-metrics.scss',
})
export class DashboardMetrics {
  @Input() stats: DashboardStats | null = null;

  get categoryEntries(): { label: string; count: number; percentage: number }[] {
    const failures = this.stats?.failuresByCategory ?? {};
    const values = Object.values(failures);
    const max = Math.max(1, ...values);
    return Object.entries(failures).map(([key, count]) => ({
      label: CATEGORY_LABELS[key] ?? key,
      count,
      percentage: Math.round((count / max) * 100),
    }));
  }

  formatDuration(seconds: number | undefined): string {
    if (!seconds) {
      return '0s';
    }
    if (seconds < 60) {
      return `${Math.round(seconds)}s`;
    }
    const minutes = Math.floor(seconds / 60);
    const rest = Math.round(seconds % 60);
    return `${minutes}m ${rest}s`;
  }
}
