import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-restriction-item',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './restriction-item.html',
  styleUrl: './restriction-item.scss',
})
export class RestrictionItem {
  @Input({ required: true }) icon!: string;
  @Input({ required: true }) title!: string;
  @Input({ required: true }) description!: string;
  @Input() actionLabel = 'Resolver';
  @Output() resolve = new EventEmitter<void>();
}
