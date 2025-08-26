import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotificationService, ToastNotification } from '../../../services/notification.service';

@Component({
  selector: 'app-toast-notification',
  template: `
    <div class="toast-container">
      <div 
        *ngFor="let toast of activeToasts" 
        class="toast" 
        [ngClass]="toast.type"
        [@slideIn]>
        <div class="toast-content">
          <div class="toast-icon">
            <span *ngIf="toast.type === 'success'">✅</span>
            <span *ngIf="toast.type === 'error'">❌</span>
            <span *ngIf="toast.type === 'warning'">⚠️</span>
            <span *ngIf="toast.type === 'info'">ℹ️</span>
            <span *ngIf="toast.type === 'achievement'">🏆</span>
            <span *ngIf="toast.type === 'celebration'">🎉</span>
          </div>
          <div class="toast-message">
            <div class="toast-title">{{ toast.title }}</div>
            <div class="toast-text">{{ toast.message }}</div>
          </div>
          <div class="toast-actions">
            <button *ngIf="toast.action" class="action-btn" (click)="executeAction(toast)">
              {{ toast.action.text }}
            </button>
            <button class="close-btn" (click)="dismissToast(toast)">×</button>
          </div>
        </div>
        <div class="toast-progress" *ngIf="toast.duration" 
             [style.animation-duration.ms]="toast.duration"></div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      pointer-events: none;
    }
    
    .toast {
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      margin-bottom: 12px;
      overflow: hidden;
      min-width: 300px;
      max-width: 400px;
      pointer-events: all;
      position: relative;
      
      &.success { border-left: 4px solid #10b981; }
      &.error { border-left: 4px solid #ef4444; }
      &.warning { border-left: 4px solid #f59e0b; }
      &.info { border-left: 4px solid #3b82f6; }
      &.achievement { 
        border-left: 4px solid #8b5cf6;
        background: linear-gradient(135deg, #f3e8ff 0%, #e0e7ff 100%);
      }
      &.celebration {
        border-left: 4px solid #f59e0b;
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      }
      
      .toast-content {
        display: flex;
        align-items: flex-start;
        padding: 16px;
        gap: 12px;
        
        .toast-icon {
          font-size: 20px;
          flex-shrink: 0;
        }
        
        .toast-message {
          flex: 1;
          
          .toast-title {
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 4px;
          }
          
          .toast-text {
            font-size: 14px;
            color: #4b5563;
            line-height: 1.4;
          }
        }
        
        .toast-actions {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-shrink: 0;
          
          .action-btn {
            background: #3b82f6;
            color: white;
            border: none;
            border-radius: 4px;
            padding: 4px 8px;
            font-size: 12px;
            cursor: pointer;
            
            &:hover {
              background: #2563eb;
            }
          }
          
          .close-btn {
            background: none;
            border: none;
            font-size: 18px;
            cursor: pointer;
            color: #9ca3af;
            padding: 0;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            
            &:hover {
              color: #4b5563;
            }
          }
        }
      }
      
      .toast-progress {
        position: absolute;
        bottom: 0;
        left: 0;
        height: 3px;
        background: currentColor;
        animation: progress linear;
      }
    }
    
    @keyframes progress {
      from { width: 100%; }
      to { width: 0%; }
    }
  `],
  animations: [
    // Define slideIn animation here or import from @angular/animations
  ]
})
export class ToastNotificationComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  activeToasts: (ToastNotification & { id: string })[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.toast$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(toast => {
      if (toast) {
        this.addToast(toast);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private addToast(toast: ToastNotification): void {
    const toastWithId = {
      ...toast,
      id: this.generateId()
    };
    
    this.activeToasts.push(toastWithId);
    
    // Auto dismiss
    if (toast.duration !== 0) {
      setTimeout(() => {
        this.removeToast(toastWithId.id);
      }, toast.duration || 5000);
    }
  }

  dismissToast(toast: any): void {
    this.removeToast(toast.id);
  }

  executeAction(toast: any): void {
    if (toast.action?.callback) {
      toast.action.callback();
    }
    this.removeToast(toast.id);
  }

  private removeToast(id: string): void {
    this.activeToasts = this.activeToasts.filter(t => t.id !== id);
  }

  private generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }
}