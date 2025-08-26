import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-unauthorized',
  template: `
    <div class="error-page">
      <div class="error-content">
        <div class="error-illustration">
          <div class="lock-icon">🔒</div>
          <div class="warning-icon">⚠️</div>
        </div>
        
        <div class="error-message">
          <h1>Access Denied</h1>
          <p class="error-description">
            You don't have permission to access this page. 
            Please contact your administrator if you believe this is an error.
          </p>
          
          <div class="error-details">
            <div class="detail-item">
              <span class="detail-label">Error Code:</span>
              <span class="detail-value">403 - Forbidden</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Your Role:</span>
              <span class="detail-value">{{ userRole || 'Unknown' }}</span>
            </div>
          </div>
        </div>
        
        <div class="error-actions">
          <button class="btn btn-primary" (click)="goToDashboard()">
            📊 Go to Dashboard
          </button>
          <button class="btn btn-secondary" (click)="goBack()">
            ← Go Back
          </button>
          <button class="btn btn-outline" (click)="logout()">
            🚪 Logout
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .error-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }
    
    .error-content {
      background: white;
      border-radius: 16px;
      padding: 48px;
      text-align: center;
      max-width: 500px;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    }
    
    .error-illustration {
      position: relative;
      margin-bottom: 32px;
      
      .lock-icon {
        font-size: 80px;
        opacity: 0.3;
      }
      
      .warning-icon {
        position: absolute;
        top: -10px;
        right: 40%;
        font-size: 24px;
        animation: bounce 2s infinite;
      }
    }
    
    .error-message {
      margin-bottom: 32px;
      
      h1 {
        color: #1f2937;
        font-size: 32px;
        margin-bottom: 16px;
      }
      
      .error-description {
        color: #6b7280;
        font-size: 16px;
        line-height: 1.6;
        margin-bottom: 24px;
      }
      
      .error-details {
        background: #f9fafb;
        border-radius: 8px;
        padding: 16px;
        
        .detail-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .detail-label {
            font-weight: 500;
            color: #374151;
          }
          
          .detail-value {
            color: #6b7280;
            font-family: monospace;
          }
        }
      }
    }
    
    .error-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      flex-wrap: wrap;
    }
    
    @keyframes bounce {
      0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
      40% { transform: translateY(-10px); }
      60% { transform: translateY(-5px); }
    }
    
    @media (max-width: 768px) {
      .error-content {
        padding: 32px 24px;
      }
      
      .error-illustration .lock-icon {
        font-size: 60px;
      }
      
      .error-message h1 {
        font-size: 24px;
      }
      
      .error-actions {
        flex-direction: column;
        align-items: center;
        
        .btn {
          width: 100%;
        }
      }
    }
  `]
})
export class UnauthorizedComponent {
  userRole: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {
    const user = this.authService.getCurrentUser();
    this.userRole = user?.role || 'Unknown';
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goBack(): void {
    window.history.back();
  }

  logout(): void {
    this.authService.logout();
  }
}