import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Subscription, forkJoin } from 'rxjs';

import { DashboardMetrics } from '../../components/dashboard-metrics/dashboard-metrics';
import { HistoryTable } from '../../components/history-table/history-table';
import { DashboardStats, RankingEntry } from '../../core/models/dashboard.model';
import { Session } from '../../core/models/session.model';
import { DashboardSocketService } from '../../core/services/dashboard-socket.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [DashboardMetrics, HistoryTable],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardPage implements OnInit, OnDestroy {
  private readonly dashboardService = inject(DashboardService);
  private readonly sessionService = inject(SessionService);
  private readonly dashboardSocket = inject(DashboardSocketService);

  stats = signal<DashboardStats | null>(null);
  ranking = signal<RankingEntry[]>([]);
  sessions = signal<Session[]>([]);
  readonly exportUrl = this.sessionService.exportCsvUrl();

  private socketSubscription?: Subscription;

  ngOnInit(): void {
    this.loadData();
    this.socketSubscription = this.dashboardSocket.connect().subscribe(() => this.loadData());
  }

  ngOnDestroy(): void {
    this.socketSubscription?.unsubscribe();
    this.dashboardSocket.disconnect();
  }

  private loadData(): void {
    forkJoin({
      stats: this.dashboardService.getStats(),
      ranking: this.dashboardService.getRanking(),
      sessions: this.sessionService.listSessions(),
    }).subscribe(({ stats, ranking, sessions }) => {
      this.stats.set(stats);
      this.ranking.set(ranking);
      this.sessions.set(sessions);
    });
  }
}
