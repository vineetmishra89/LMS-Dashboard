import { Injectable } from '@angular/core';
import { Observable, timer, EMPTY, BehaviorSubject } from 'rxjs';
import { switchMap, retry, catchError, tap } from 'rxjs/operators';
import { WebSocketService } from './websocket.service';
import { UserService } from './user.service';
import { EnrollmentService } from './enrollment.service';
import { NotificationService } from './notification.service';
import { StorageService } from './storage.service';

interface SyncStatus {
  isOnline: boolean;
  lastSync: Date | null;
  pendingSync: boolean;
  syncErrors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class DataSyncService {
  private syncInterval = 30000; // 30 seconds
  private syncStatusSubject = new BehaviorSubject<SyncStatus>({
    isOnline: navigator.onLine,
    lastSync: null,
    pendingSync: false,
    syncErrors: []
  });

  public syncStatus$ = this.syncStatusSubject.asObservable();

  constructor(
    private webSocketService: WebSocketService,
    private userService: UserService,
    private enrollmentService: EnrollmentService,
    private notificationService: NotificationService,
    private storageService: StorageService
  ) {
    this.setupOnlineStatusListener();
    this.setupWebSocketListeners();
    this.startPeriodicSync();
    this.loadPendingSync();
  }

  private setupOnlineStatusListener(): void {
    window.addEventListener('online', () => {
      this.updateSyncStatus({ isOnline: true });
      this.syncOfflineData();
    });

    window.addEventListener('offline', () => {
      this.updateSyncStatus({ isOnline: false });
    });
  }

  private setupWebSocketListeners(): void {
    this.webSocketService.messages$.subscribe(message => {
      if (message) {
        this.handleRealtimeUpdate(message);
      }
    });

    this.webSocketService.connectionStatus$.subscribe(status => {
      console.log('WebSocket status:', status);
    });
  }

  private startPeriodicSync(): void {
    timer(0, this.syncInterval).pipe(
      switchMap(() => {
        const status = this.syncStatusSubject.value;
        if (status.isOnline && !status.pendingSync) {
          return this.syncUserData();
        }
        return EMPTY;
      }),
      retry(3),
      catchError(error => {
        console.warn('Periodic sync failed:', error);
        this.addSyncError('Periodic sync failed: ' + error.message);
        return EMPTY;
      })
    ).subscribe({
      next: () => {
        this.updateSyncStatus({ 
          lastSync: new Date(),
          syncErrors: []
        });
      }
    });
  }

  private syncUserData(): Observable<any> {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return EMPTY;

    this.updateSyncStatus({ pendingSync: true });

    return this.enrollmentService.getUserEnrollments(currentUser.id).pipe(
      switchMap(() => this.notificationService.getUserNotifications(currentUser.id)),
      tap(() => {
        this.updateSyncStatus({ pendingSync: false });
      }),
      catchError(error => {
        this.updateSyncStatus({ pendingSync: false });
        this.addSyncError('User data sync failed: ' + error.message);
        throw error;
      })
    );
  }

  private syncOfflineData(): void {
    this.enrollmentService.syncOfflineProgress().subscribe({
      next: () => {
        this.notificationService.showSuccessNotification(
          'Your offline progress has been synced successfully!',
          'Sync Complete'
        );
        this.clearPendingSync();
      },
      error: (error) => {
        console.error('Failed to sync offline data:', error);
        this.addSyncError('Offline sync failed: ' + error.message);
      }
    });
  }

  private handleRealtimeUpdate(message: any): void {
    console.log('Handling real-time update:', message);
    
    switch (message.type) {
      case 'enrollment_update':
        this.handleEnrollmentUpdate(message.data);
        break;
      case 'new_notification':
        this.handleNewNotification(message.data);
        break;
      case 'course_update':
        this.handleCourseUpdate(message.data);
        break;
      case 'achievement_earned':
        this.handleAchievementEarned(message.data);
        break;
      case 'user_updated':
        this.handleUserUpdate(message.data);
        break;
      default:
        console.log('Unknown real-time message type:', message.type);
    }
  }

  private handleEnrollmentUpdate(data: any): void {
    // Refresh specific enrollment data
    this.enrollmentService.getEnrollmentById(data.enrollmentId).subscribe({
      next: (enrollment) => {
        console.log('Enrollment updated via real-time:', enrollment);
      },
      error: (error) => {
        console.error('Failed to refresh enrollment:', error);
      }
    });
  }

  private handleNewNotification(notification: any): void {
    this.notificationService.showNotification({
      title: notification.title,
      message: notification.message,
      type: 'info'
    });

    // Refresh notifications list
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) {
      this.notificationService.getUserNotifications(currentUser.id).subscribe();
    }
  }

  private handleCourseUpdate(data: any): void {
    this.notificationService.showNotification({
      title: 'Course Updated',
      message: `${data.courseTitle} has new content available!`,
      type: 'info',
      action: {
        text: 'View Course',
        callback: () => {
          // Navigate to course
          console.log('Navigate to course:', data.courseId);
        }
      }
    });
  }

  private handleAchievementEarned(data: any): void {
    this.notificationService.showAchievementNotification({
      title: `🏆 ${data.achievementTitle}`,
      message: data.achievementDescription,
      duration: 10000
    });
  }

  private handleUserUpdate(data: any): void {
    // Refresh user data
    const currentUser = this.userService.getCurrentUser();
    if (currentUser && data.userId === currentUser.id) {
      this.userService.getUserById(data.userId).subscribe();
    }
  }

  // Public API methods
  forceSyncAll(): Observable<any> {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return EMPTY;

    this.updateSyncStatus({ pendingSync: true });
    
    return this.syncUserData().pipe(
      tap(() => {
        this.notificationService.showSuccessNotification('Data synchronized successfully!');
      }),
      catchError(error => {
        this.notificationService.showErrorNotification('Sync failed. Please try again.');
        throw error;
      })
    );
  }

  private updateSyncStatus(updates: Partial<SyncStatus>): void {
    const currentStatus = this.syncStatusSubject.value;
    this.syncStatusSubject.next({ ...currentStatus, ...updates });
  }

  private addSyncError(error: string): void {
    const currentStatus = this.syncStatusSubject.value;
    const errors = [...currentStatus.syncErrors, error].slice(-5); // Keep last 5 errors
    this.updateSyncStatus({ syncErrors: errors });
  }

  private loadPendingSync(): void {
    const pending = this.storageService.getItem('pendingSync');
    if (pending) {
      this.updateSyncStatus({ pendingSync: true });
    }
  }

  private clearPendingSync(): void {
    this.storageService.removeItem('pendingSync');
    this.updateSyncStatus({ pendingSync: false });
  }

  getSyncStatus(): SyncStatus {
    return this.syncStatusSubject.value;
  }

  isDataStale(lastUpdate: Date, maxAge: number = 300000): boolean {
    return Date.now() - lastUpdate.getTime() > maxAge; // 5 minutes default
  }
}