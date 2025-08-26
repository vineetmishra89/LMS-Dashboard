import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  template: `
    <div class="loading-container" [ngClass]="size">
      <div class="spinner" [ngClass]="color"></div>
      <div class="loading-text" *ngIf="message">{{ message }}</div>
    </div>
  `,
  styles: [`
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;
      
      &.small { padding: 10px; }
      &.large { padding: 40px; }
    }
    
    .spinner {
      width: 32px;
      height: 32px;
      border: 3px solid #f3f4f6;
      border-top: 3px solid #3b82f6;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      
      &.primary { border-top-color: #3b82f6; }
      &.secondary { border-top-color: #6b7280; }
      &.success { border-top-color: #10b981; }
      &.warning { border-top-color: #f59e0b; }
      &.error { border-top-color: #ef4444; }
      
      .small & { width: 20px; height: 20px; border-width: 2px; }
      .large & { width: 48px; height: 48px; border-width: 4px; }
    }
    
    .loading-text {
      margin-top: 12px;
      color: #6b7280;
      font-size: 14px;
      text-align: center;
    }
    
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() color: 'primary' | 'secondary' | 'success' | 'warning' | 'error' = 'primary';
  @Input() message: string = '';
}