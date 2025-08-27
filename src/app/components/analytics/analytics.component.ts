import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { UserAnalytics, WeeklyActivity } from '../../models/analytics';
import { AnalyticsService } from '../../services/analytics.service';
import { UserService } from '../../services/user.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.scss']
})
export class AnalyticsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  analytics: UserAnalytics | null = null;
  weeklyActivity: WeeklyActivity[] = [];
  isLoading = true;
  selectedPeriod = '3months';
  
  // Chart data
  progressChartData: any = null;
  activityChartData: any = null;
  skillsChartData: any = null;
  
  // Summary stats
  summaryStats = {
    totalStudyTime: 0,
    averageDailyTime: 0,
    coursesInProgress: 0,
    upcomingDeadlines: 0,
    weeklyGoalProgress: 0
  };

  constructor(
    private analyticsService: AnalyticsService,
    private userService: UserService,
    private enrollmentService: EnrollmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAnalyticsData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAnalyticsData(): void {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return;

    this.isLoading = true;
    
    combineLatest([
      this.analyticsService.getUserAnalytics(currentUser.id),
      this.analyticsService.getWeeklyActivity(currentUser.id, 12),
      this.enrollmentService.getUserEnrollments(currentUser.id, { status: ['active'] })
    ]).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: ([analytics, weeklyActivity, activeEnrollments]) => {
        this.analytics = analytics;
        this.weeklyActivity = weeklyActivity;
        this.calculateSummaryStats(analytics, activeEnrollments || []);
        this.prepareChartData();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load analytics:', error);
        this.isLoading = false;
      }
    });
  }

  private calculateSummaryStats(analytics: UserAnalytics, activeEnrollments: any[]): void {
    const totalMinutes = analytics.totalHoursLearned * 60;
    const daysActive = analytics.weeklyActivity?.length * 7 || 30;
    
    this.summaryStats = {
      totalStudyTime: analytics.totalHoursLearned,
      averageDailyTime: Math.round(totalMinutes / daysActive),
      coursesInProgress: activeEnrollments.length,
      upcomingDeadlines: 3, // This would come from actual deadline data
      weeklyGoalProgress: 75 // This would be calculated from user's goals
    };
  }

  private prepareChartData(): void {
    // Prepare data for charts (would integrate with Chart.js or similar)
    this.progressChartData = {
      labels: this.analytics?.learningPath.map(lp => lp.pathName) || [],
      datasets: [{
        label: 'Progress',
        data: this.analytics?.learningPath.map(lp => lp.progress) || [],
        backgroundColor: '#3b82f6'
      }]
    };

    this.activityChartData = {
      labels: this.weeklyActivity.map(wa => wa.week),
      datasets: [{
        label: 'Hours Studied',
        data: this.weeklyActivity.map(wa => wa.hoursSpent),
        borderColor: '#10b981',
        fill: false
      }]
    };

    this.skillsChartData = {
      labels: this.analytics?.skillsAcquired || [],
      datasets: [{
        label: 'Skills',
        data: new Array(this.analytics?.skillsAcquired.length || 0).fill(1),
        backgroundColor: [
          '#ef4444', '#f59e0b', '#10b981', '#3b82f6', 
          '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'
        ]
      }]
    };
  }

  onPeriodChange(period: string): void {
    this.selectedPeriod = period;
    this.loadAnalyticsData();
  }

  exportAnalytics(format: 'pdf' | 'csv' | 'json'): void {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser || !this.analytics) return;

    // Create export data
    const exportData = {
      user: {
        name: `${currentUser.firstName} ${currentUser.lastName}`,
        email: currentUser.email
      },
      analytics: this.analytics,
      weeklyActivity: this.weeklyActivity,
      exportedAt: new Date().toISOString(),
      period: this.selectedPeriod
    };

    if (format === 'json') {
      this.downloadJsonReport(exportData);
    } else if (format === 'csv') {
      this.downloadCsvReport(exportData);
    } else {
      // For PDF, you'd typically call a backend service
      this.notificationService.showNotification({
        title: 'Export Started',
        message: 'Your analytics report is being generated...',
        type: 'info'
      });
    }
  }

  private downloadJsonReport(data: any): void {
    const dataStr = JSON.stringify(data, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    this.downloadFile(dataBlob, `analytics-report-${new Date().toISOString().split('T')[0]}.json`);
  }

  private downloadCsvReport(data: any): void {
    const csvContent = this.convertToCSV(data.weeklyActivity);
    const dataBlob = new Blob([csvContent], { type: 'text/csv' });
    this.downloadFile(dataBlob, `analytics-report-${new Date().toISOString().split('T')[0]}.csv`);
  }

  private convertToCSV(weeklyData: WeeklyActivity[]): string {
    const headers = 'Week,Hours Spent,Lessons Completed,Quizzes Attempted\n';
    const rows = weeklyData.map(week => 
      `${week.week},${week.hoursSpent},${week.lessonsCompleted},${week.quizzesAttempted}`
    ).join('\n');
    return headers + rows;
  }

  private downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
    
    this.notificationService.showSuccessNotification('Report downloaded successfully!');
  }

  refreshAnalytics(): void {
    this.loadAnalyticsData();
  }
}