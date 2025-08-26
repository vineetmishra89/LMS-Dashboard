import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';
import { tap } from 'rxjs/operators';

export interface Notification {
  id: string;
  userId: string;
  type: 'course_update' | 'assignment_due' | 'certificate_earned' | 'achievement' | 'system';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: Date;
  data?: any;
}

export interface AppNotification {
  id: string;
  userId: string;
  type: 'course_update' | 'assignment_due' | 'certificate_earned' | 'achievement' | 'system' | 'reminder';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: Date;
  data?: any;
  priority: 'low' | 'medium' | 'high';
  expiresAt?: Date;
}

export interface ToastNotification {
  title: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning' | 'achievement' | 'celebration';
  duration?: number;
  action?: {
    text: string;
    callback: () => void;
  };
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  private unreadCountSubject = new BehaviorSubject<number>(0);
  private toastSubject = new BehaviorSubject<ToastNotification | null>(null);

  public notifications$ = this.notificationsSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();
  public toast$ = this.toastSubject.asObservable();

  constructor(private apiService: ApiService) {}

  // Toast Notifications (In-app)
  showNotification(notification: ToastNotification): void {
    this.toastSubject.next(notification);
    
    // Auto-dismiss after duration
    const duration = notification.duration || 5000;
    setTimeout(() => {
      this.dismissToast();
    }, duration);
  }

  showAchievementNotification(notification: Omit<ToastNotification, 'type'>): void {
    this.showNotification({
      ...notification,
      type: 'achievement',
      duration: 8000 // Achievements stay longer
    });
  }

  showErrorNotification(message: string, title: string = 'Error'): void {
    this.showNotification({
      title,
      message,
      type: 'error',
      duration: 6000
    });
  }

  showSuccessNotification(message: string, title: string = 'Success'): void {
    this.showNotification({
      title,
      message,
      type: 'success',
      duration: 4000
    });
  }

  dismissToast(): void {
    this.toastSubject.next(null);
  }

  // Persistent Notifications (Database)
  getUserNotifications(userId: string, options?: {
    unreadOnly?: boolean;
    type?: string;
    limit?: number;
    offset?: number;
  }): Observable<AppNotification[]> {
    let endpoint = `users/${userId}/notifications`;
    const params: any = {};
    
    if (options?.unreadOnly) params.unreadOnly = 'true';
    if (options?.type) params.type = options.type;
    if (options?.limit) params.limit = options.limit.toString();
    if (options?.offset) params.offset = options.offset.toString();
    
    return this.apiService.get<AppNotification[]>(endpoint, params).pipe(
      tap(notifications => {
        this.notificationsSubject.next(notifications);
        this.updateUnreadCount(notifications);
      })
    );
  }

  markAsRead(notificationId: string): Observable<void> {
    return this.apiService.put<void>(`notifications/${notificationId}/read`, {}).pipe(
      tap(() => {
        // Update local notifications
        const notifications = this.notificationsSubject.value;
        const updatedNotifications = notifications.map(n => 
          n.id === notificationId ? { ...n, isRead: true } : n
        );
        this.notificationsSubject.next(updatedNotifications);
        this.updateUnreadCount(updatedNotifications);
      })
    );
  }

  markAllAsRead(userId: string): Observable<void> {
    return this.apiService.put<void>(`users/${userId}/notifications/read-all`, {}).pipe(
      tap(() => {
        // Update local notifications
        const notifications = this.notificationsSubject.value;
        const updatedNotifications = notifications.map(n => ({ ...n, isRead: true }));
        this.notificationsSubject.next(updatedNotifications);
        this.unreadCountSubject.next(0);
      })
    );
  }

  deleteNotification(notificationId: string): Observable<void> {
    return this.apiService.delete<void>(`notifications/${notificationId}`).pipe(
      tap(() => {
        const notifications = this.notificationsSubject.value;
        const updatedNotifications = notifications.filter(n => n.id !== notificationId);
        this.notificationsSubject.next(updatedNotifications);
        this.updateUnreadCount(updatedNotifications);
      })
    );
  }

  createNotification(userId: string, notification: Omit<AppNotification, 'id' | 'userId' | 'createdAt' | 'isRead'>): Observable<AppNotification> {
    return this.apiService.post<AppNotification>('notifications', {
      ...notification,
      userId
    });
  }

  // Push Notifications
  requestPermission(): Promise<NotificationPermission> {
    if (!('Notification' in window)) {
      console.warn('This browser does not support notifications');
      return Promise.resolve('denied');
    }
    
    return Notification.requestPermission();
  }

  sendPushNotification(title: string, options?: NotificationOptions): void {
    if (Notification.permission === 'granted') {
      const notification = new Notification(title, {
        icon: '/assets/icons/icon-192x192.png',
        badge: '/assets/icons/badge-72x72.png',
        ...options
      });
      
      notification.onclick = () => {
        window.focus();
        notification.close();
      };
    }
  }

  // Utility Methods
  private updateUnreadCount(notifications: AppNotification[]): void {
    const unreadCount = notifications.filter(n => !n.isRead).length;
    this.unreadCountSubject.next(unreadCount);
  }

  getUnreadCount(): number {
    return this.unreadCountSubject.value;
  }

  // Study Reminders
  scheduleStudyReminder(userId: string, courseId: string, reminderTime: Date): Observable<void> {
    return this.apiService.post<void>('reminders', {
      userId,
      courseId,
      scheduledFor: reminderTime.toISOString(),
      type: 'study_reminder'
    });
  }

  cancelStudyReminder(reminderId: string): Observable<void> {
    return this.apiService.delete<void>(`reminders/${reminderId}`);
  }
}