import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotificationService, AppNotification } from '../../services/notification.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  notifications: AppNotification[] = [];
  filteredNotifications: AppNotification[] = [];
  isLoading = true;
  activeFilter = 'all';
  unreadCount = 0;
  
  filters = [
    { key: 'all', label: 'All', count: 0 },
    { key: 'unread', label: 'Unread', count: 0 },
    { key: 'course_update', label: 'Course Updates', count: 0 },
    { key: 'achievement', label: 'Achievements', count: 0 },
    { key: 'system', label: 'System', count: 0 }
  ];

  constructor(
    private notificationService: NotificationService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.subscribeToUnreadCount();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadNotifications(): void {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return;

    this.isLoading = true;
    
    this.notificationService.getUserNotifications(currentUser.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.updateFilterCounts();
        this.applyFilter(this.activeFilter);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load notifications:', error);
        this.isLoading = false;
      }
    });
  }

  private subscribeToUnreadCount(): void {
    this.notificationService.unreadCount$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(count => {
      this.unreadCount = count;
    });
  }

  private updateFilterCounts(): void {
    this.filters.forEach(filter => {
      switch (filter.key) {
        case 'all':
          filter.count = this.notifications.length;
          break;
        case 'unread':
          filter.count = this.notifications.filter(n => !n.isRead).length;
          break;
        default:
          filter.count = this.notifications.filter(n => n.type === filter.key).length;
      }
    });
  }

  setActiveFilter(filterKey: string): void {
    this.activeFilter = filterKey;
    this.applyFilter(filterKey);
  }

  private applyFilter(filterKey: string): void {
    switch (filterKey) {
      case 'all':
        this.filteredNotifications = this.notifications;
        break;
      case 'unread':
        this.filteredNotifications = this.notifications.filter(n => !n.isRead);
        break;
      default:
        this.filteredNotifications = this.notifications.filter(n => n.type === filterKey);
    }
  }

  markAsRead(notification: AppNotification): void {
    if (notification.isRead) return;
    
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.isRead = true;
        this.updateFilterCounts();
      },
      error: (error) => {
        console.error('Failed to mark as read:', error);
      }
    });
  }

  markAllAsRead(): void {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return;

    this.notificationService.markAllAsRead(currentUser.id).subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
        this.updateFilterCounts();
        this.applyFilter(this.activeFilter);
      },
      error: (error) => {
        console.error('Failed to mark all as read:', error);
      }
    });
  }

  deleteNotification(notification: AppNotification): void {
    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
        this.updateFilterCounts();
        this.applyFilter(this.activeFilter);
      },
      error: (error) => {
        console.error('Failed to delete notification:', error);
      }
    });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'course_update': return '📚';
      case 'assignment_due': return '⏰';
      case 'certificate_earned': return '🏆';
      case 'achievement': return '🎯';
      case 'system': return '⚙️';
      case 'reminder': return '🔔';
      default: return 'ℹ️';
    }
  }

  getTimeAgo(date: Date): string {
    const now = new Date();
    const diffMs = now.getTime() - new Date(date).getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return new Date(date).toLocaleDateString();
  }
}
