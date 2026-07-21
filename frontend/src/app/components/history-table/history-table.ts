import { DatePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';

import { RankingEntry } from '../../core/models/dashboard.model';
import { Session } from '../../core/models/session.model';

@Component({
  selector: 'app-history-table',
  standalone: true,
  imports: [MatTableModule, MatButtonModule, MatIconModule, DatePipe],
  templateUrl: './history-table.html',
  styleUrl: './history-table.scss',
})
export class HistoryTable {
  @Input() sessions: Session[] = [];
  @Input() ranking: RankingEntry[] = [];
  @Input() exportUrl = '';

  readonly sessionColumns = ['id', 'userName', 'score', 'result', 'createdAt'];
  readonly rankingColumns = ['position', 'userName', 'averageScore', 'sessionsCount'];
}
