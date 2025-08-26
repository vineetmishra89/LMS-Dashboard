import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-error-display',
  template: `
    <div class="error-container" [ngClass]="type">
      <div class="error-content">
        <div class="error-icon">
          <span *ngIf="type === 'error'">⚠️</span>
          <span *ngIf="type === 'warning'">⚡</span>
          <span *ngIf="type === 'info'">ℹ️</span>
        </div>
        <div class="error-message">
          <h4 *ngIf="title">{{ title }}</h4>
          <p>{{ message }}</p>
          <div class="error-details" *ngIf="details">
            <button class="details-toggle" (click)="showDetails = !showDetails">
              {{ showDetails ? 'Hide' : 'Show' }} Details
            </button>
            <div class="details-content" *ngIf="showDetails">
              <pre>{{ details }}</pre>
            </div>
          </div>
        </div>
        <div class="error-actions" *ngIf="showRetry || showDismiss">
          <button *ngIf="showRetry" class="btn btn-primary" (click)="onRetry()">
            Try Again
          </button>
          <button *ngIf="showDismiss" class="btn btn-secondary" (click)="onDismiss()">
            Dismiss
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .error-container {
      border-radius: 8px;
      padding: 16px;
      margin: 16px 0;
      border-left: 4px solid;
      
      &.error {
        background: #fef2f2;
        border-color: #ef4444;
        color: #7f1d1d;
      }
      
      &.warning {
        background: #fffbeb;
        border-color: #f59e0b;
        color: #92400e;
      }
      
      &.info {
        background: #eff6ff;
        border-color: #3b82f6;
        color: #1e40af;
      }
    }
    
    .error-content {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      
      .error-icon {
        font-size: 20px;
        flex-shrink: 0;
      }
      
      .error-message {
        flex: 1;
        
        h4 {
          margin: 0 0 4px 0;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          line-height: 1.4;
        }
        
        .error-details {
          margin-top: 8px;
          
          .details-toggle {
            background: none;
            border: none;
            color: inherit;
            text-decoration: underline;
            cursor: pointer;
            font-size: 12px;
            
            &:hover {
              opacity: 0.8;
            }
          }
          
          .details-content {
            margin-top: 8px;
            padding: 8px;
            background: rgba(0, 0, 0, 0.05);
            border-radius: 4px;
            
            pre {
              margin: 0;
              font-size: 11px;
              white-space: pre-wrap;
              word-break: break-all;
            }
          }
        }
      }
      
      .error-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
        
        .btn {
          padding: 6px 12px;
          font-size: 12px;
        }
      }
    }
  `]
})
export class ErrorDisplayComponent {
  @Input() type: 'error' | 'warning' | 'info' = 'error';
  @Input() title: string = '';
  @Input() message: string = '';
  @Input() details: string = '';
  @Input() showRetry: boolean = true;
  @Input() showDismiss: boolean = true;
  
  @Output() retry = new EventEmitter<void>();
  @Output() dismiss = new EventEmitter<void>();

  showDetails = false;

  onRetry(): void {
    this.retry.emit();
  }

  onDismiss(): void {
    this.dismiss.emit();
  }
}
