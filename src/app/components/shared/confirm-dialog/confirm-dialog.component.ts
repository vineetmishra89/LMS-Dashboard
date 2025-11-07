import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="dialog-overlay" *ngIf="isVisible" (click)="onOverlayClick()">
      <div class="dialog-container" (click)="$event.stopPropagation()">
        <div class="dialog-header" *ngIf="title">
          <h3>{{ title }}</h3>
          <button class="close-btn" (click)="onCancel()">×</button>
        </div>
        
        <div class="dialog-body">
          <div class="dialog-icon" *ngIf="type">
            <span *ngIf="type === 'danger'">⚠️</span>
            <span *ngIf="type === 'warning'">⚡</span>
            <span *ngIf="type === 'info'">ℹ️</span>
            <span *ngIf="type === 'success'">✅</span>
          </div>
          <div class="dialog-content">
            <p>{{ message }}</p>
            <div class="dialog-details" *ngIf="details">
              <small>{{ details }}</small>
            </div>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button class="btn btn-secondary" (click)="onCancel()" [disabled]="isProcessing">
            {{ cancelText }}
          </button>
          <button class="btn" [ngClass]="confirmButtonClass" (click)="onConfirm()" [disabled]="isProcessing">
            <span *ngIf="!isProcessing">{{ confirmText }}</span>
            <span *ngIf="isProcessing" class="loading-content">
              <app-loading-spinner size="small" color="secondary"></app-loading-spinner>
              {{ processingText }}
            </span>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dialog-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10000;
      animation: fadeIn 0.2s ease;
    }
    
    .dialog-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
      min-width: 400px;
      max-width: 500px;
      animation: slideIn 0.3s ease;
    }
    
    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 24px 0;
      
      h3 {
        margin: 0;
        color: #1f2937;
        font-size: 18px;
      }
      
      .close-btn {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        color: #9ca3af;
        padding: 0;
        
        &:hover {
          color: #4b5563;
        }
      }
    }
    
    .dialog-body {
      padding: 20px 24px;
      display: flex;
      gap: 16px;
      
      .dialog-icon {
        font-size: 32px;
        flex-shrink: 0;
      }
      
      .dialog-content {
        flex: 1;
        
        p {
          margin: 0 0 8px 0;
          color: #374151;
          line-height: 1.5;
        }
        
        .dialog-details {
          small {
            color: #6b7280;
            line-height: 1.4;
          }
        }
      }
    }
    
    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding: 0 24px 24px;
      
      .btn {
        min-width: 80px;
        
        &.btn-danger {
          background: #ef4444;
          
          &:hover {
            background: #dc2626;
          }
        }
        
        .loading-content {
          display: flex;
          align-items: center;
          gap: 8px;
          justify-content: center;
        }
      }
    }
    
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    
    @keyframes slideIn {
      from { transform: scale(0.9) translateY(-10px); opacity: 0; }
      to { transform: scale(1) translateY(0); opacity: 1; }
    }
    
    @media (max-width: 768px) {
      .dialog-container {
        min-width: 90%;
        margin: 20px;
      }
      
      .dialog-body {
        flex-direction: column;
        text-align: center;
      }
    }
  `]
})
export class ConfirmDialogComponent {
  @Input() isVisible: boolean = false;
  @Input() title: string = 'Confirm Action';
  @Input() message: string = 'Are you sure you want to continue?';
  @Input() details: string = '';
  @Input() type: 'danger' | 'warning' | 'info' | 'success' = 'info';
  @Input() confirmText: string = 'Confirm';
  @Input() cancelText: string = 'Cancel';
  @Input() processingText: string = 'Processing...';
  @Input() isProcessing: boolean = false;
  
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  get confirmButtonClass(): string {
    switch (this.type) {
      case 'danger': return 'btn-danger';
      case 'warning': return 'btn-warning';
      case 'success': return 'btn-success';
      default: return 'btn-primary';
    }
  }

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onOverlayClick(): void {
    if (!this.isProcessing) {
      this.onCancel();
    }
  }
}