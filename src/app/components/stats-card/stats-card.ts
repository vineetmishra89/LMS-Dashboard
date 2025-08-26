import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stats-card',
  templateUrl: './stats-card.html',
  styleUrls: ['./stats-card.scss']
})
export class StatsCardComponent {
  @Input() icon: string = '';
  @Input() iconClass: string = '';
  @Input() label: string = '';
  @Input() value: string | number = '';
  @Input() loading: boolean = false;
  @Input() trend?: 'up' | 'down' | 'neutral';
  @Input() trendValue?: number;
}