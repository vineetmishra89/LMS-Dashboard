// src/app/services/enrollment.service.ts
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, throwError, timer } from 'rxjs';
import { tap, map, catchError, switchMap, retry } from 'rxjs/operators';
import { HttpParams } from '@angular/common/http';
import { Enrollment, EnrollmentProgress, QuizScore } from '../models/enrollment';
import { Course } from '../models/course';
import { ApiService } from './api.service';
import { NotificationService } from './notification.service';
import { AnalyticsService } from './analytics.service';

export interface EnrollmentRequest {
  userId: string;
  courseId: string;
  paymentMethod?: 'free' | 'stripe' | 'paypal';
  paymentToken?: string;
  discountCode?: string;
}

export interface LessonProgress {
  lessonId: string;
  timeSpent: number;
  isCompleted: boolean;
  completedAt?: Date;
  watchTime?: number;
  interactions?: any[];
}

export interface ModuleProgress {
  moduleId: string;
  lessonsCompleted: string[];
  totalLessons: number;
  timeSpent: number;
  isCompleted: boolean;
  completedAt?: Date;
  quizScore?: QuizScore;
}

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  private enrollmentsSubject = new BehaviorSubject<Enrollment[]>([]);
  private currentEnrollmentSubject = new BehaviorSubject<Enrollment | null>(null);
  private enrollmentStatsSubject = new BehaviorSubject<any>(null);

  public enrollments$ = this.enrollmentsSubject.asObservable();
  public currentEnrollment$ = this.currentEnrollmentSubject.asObservable();
  public enrollmentStats$ = this.enrollmentStatsSubject.asObservable();

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private analyticsService: AnalyticsService
  ) {}

  // ENROLLMENT MANAGEMENT
  enrollInCourse(enrollmentRequest: EnrollmentRequest): Observable<Enrollment> {
    return this.apiService.post<Enrollment>('enrollments', enrollmentRequest).pipe(
      tap(enrollment => {
        // Update local enrollments list
        const currentEnrollments = this.enrollmentsSubject.value;
        this.enrollmentsSubject.next([...currentEnrollments, enrollment]);
        
        // Track enrollment event
        this.analyticsService.trackEvent(
          enrollmentRequest.userId, 
          'course_enrolled', 
          { courseId: enrollmentRequest.courseId, enrollmentId: enrollment.id }
        ).subscribe();
        
        // Send welcome notification
        this.sendEnrollmentNotification(enrollment);
      }),
      catchError(error => {
        console.error('Enrollment failed:', error);
        return throwError(() => new Error('Failed to enroll in course. Please try again.'));
      })
    );
  }

  getUserEnrollments(userId: string, options?: {
    status?: string[];
    includeCompleted?: boolean;
    sortBy?: 'enrolledAt' | 'lastAccessed' | 'progress';
    limit?: number;
  }): Observable<Enrollment[]> {
    let params = new HttpParams();
    
    if (options?.status?.length) {
      params = params.set('status', options.status.join(','));
    }
    if (options?.includeCompleted !== undefined) {
      params = params.set('includeCompleted', options.includeCompleted.toString());
    }
    if (options?.sortBy) {
      params = params.set('sortBy', options.sortBy);
    }
    if (options?.limit) {
      params = params.set('limit', options.limit.toString());
    }

    params = params.set('userId', userId);
    return this.apiService.get<Enrollment[]>('enrollments', params).pipe(
      tap(enrollments => {
        this.enrollmentsSubject.next(enrollments);
      }),
      catchError(error => {
        console.error('Failed to load user enrollments:', error);
        return throwError(() => error);
      })
    );
  }

  getEnrollmentById(enrollmentId: string): Observable<Enrollment> {
    return this.apiService.get<Enrollment>(`enrollments/${enrollmentId}`).pipe(
      tap(enrollment => {
        this.currentEnrollmentSubject.next(enrollment);
      })
    );
  }

  getEnrollmentWithCourse(enrollmentId: string): Observable<{ enrollment: Enrollment; course: Course }> {
    return this.apiService.get<{ enrollment: Enrollment; course: Course }>(`enrollments/${enrollmentId}/with-course`);
  }

  // PROGRESS TRACKING
  updateEnrollmentProgress(enrollmentId: string, progress: Partial<EnrollmentProgress>): Observable<Enrollment> {
    return this.apiService.put<Enrollment>(`enrollments/${enrollmentId}/progress`, progress).pipe(
      tap(updatedEnrollment => {
        this.updateLocalEnrollment(updatedEnrollment);
        
        // Track progress update
        this.analyticsService.trackEvent(
          updatedEnrollment.userId,
          'progress_updated',
          { 
            enrollmentId,
            newProgress: progress,
            overallProgress: updatedEnrollment.progress.overallProgress
          }
        ).subscribe();
      }),
      catchError(error => {
        console.error('Failed to update enrollment progress:', error);
        return throwError(() => error);
      })
    );
  }

  // LESSON MANAGEMENT
  markLessonComplete(enrollmentId: string, lessonId: string, lessonProgress: LessonProgress): Observable<Enrollment> {
    return this.apiService.post<Enrollment>(`enrollments/${enrollmentId}/lessons/${lessonId}/complete`, lessonProgress).pipe(
      tap(enrollment => {
        this.updateLocalEnrollment(enrollment);
        
        // Check if this lesson completion triggers module completion
        this.checkModuleCompletion(enrollment, lessonProgress.lessonId);
        
        // Track lesson completion
        this.analyticsService.trackEvent(
          enrollment.userId,
          'lesson_completed',
          { enrollmentId, lessonId, timeSpent: lessonProgress.timeSpent }
        ).subscribe();
      }),
      retry(2),
      catchError(error => {
        console.error('Failed to mark lesson as complete:', error);
        return throwError(() => error);
      })
    );
  }

  updateLessonProgress(enrollmentId: string, lessonId: string, progressData: {
    timeSpent: number;
    watchTime?: number;
    interactions?: any[];
    bookmark?: number;
  }): Observable<Enrollment> {
    return this.apiService.put<Enrollment>(`enrollments/${enrollmentId}/lessons/${lessonId}/progress`, progressData).pipe(
      // Auto-save progress every 30 seconds
      switchMap(() => timer(30000).pipe(
        switchMap(() => this.getEnrollmentById(enrollmentId))
      )),
      catchError(error => {
        console.warn('Failed to update lesson progress:', error);
        return throwError(() => error);
      })
    );
  }

  getLessonProgress(enrollmentId: string, lessonId: string): Observable<LessonProgress> {
    return this.apiService.get<LessonProgress>(`enrollments/${enrollmentId}/lessons/${lessonId}/progress`);
  }

  // MODULE MANAGEMENT
  markModuleComplete(enrollmentId: string, moduleId: string): Observable<Enrollment> {
    return this.apiService.post<Enrollment>(`enrollments/${enrollmentId}/modules/${moduleId}/complete`, {}).pipe(
      tap(enrollment => {
        this.updateLocalEnrollment(enrollment);
        
        // Check if this module completion triggers course completion
        this.checkCourseCompletion(enrollment);
        
        // Track module completion
        this.analyticsService.trackEvent(
          enrollment.userId,
          'module_completed',
          { enrollmentId, moduleId }
        ).subscribe();
        
        // Show achievement notification
        this.notificationService.showAchievementNotification({
          title: 'Module Completed! 🎉',
          message: `Great job completing this module! You're making excellent progress.`
        });
      }),
      catchError(error => {
        console.error('Failed to mark module as complete:', error);
        return throwError(() => error);
      })
    );
  }

  getModuleProgress(enrollmentId: string, moduleId: string): Observable<ModuleProgress> {
    return this.apiService.get<ModuleProgress>(`enrollments/${enrollmentId}/modules/${moduleId}/progress`);
  }

  // QUIZ MANAGEMENT
  submitQuizAttempt(enrollmentId: string, quizId: string, answers: any[]): Observable<QuizScore> {
    return this.apiService.post<QuizScore>(`enrollments/${enrollmentId}/quizzes/${quizId}/attempt`, { answers }).pipe(
      tap(quizScore => {
        // Update enrollment with new quiz score
        this.updateQuizScore(enrollmentId, quizScore);
        
        // Track quiz completion
        this.analyticsService.trackEvent(
          '', // Will be filled by interceptor
          'quiz_completed',
          { enrollmentId, quizId, score: quizScore.score, maxScore: quizScore.maxScore }
        ).subscribe();
        
        // Show score notification
        const percentage = Math.round((quizScore.score / quizScore.maxScore) * 100);
        this.notificationService.showNotification({
          title: percentage >= 80 ? 'Great Quiz Score! 🎯' : 'Quiz Complete ✅',
          message: `You scored ${percentage}% on this quiz.`,
          type: percentage >= 80 ? 'success' : 'info'
        });
      })
    );
  }

  getQuizAttempts(enrollmentId: string, quizId: string): Observable<QuizScore[]> {
    return this.apiService.get<QuizScore[]>(`enrollments/${enrollmentId}/quizzes/${quizId}/attempts`);
  }

  // COURSE COMPLETION
  completeCourse(enrollmentId: string): Observable<Enrollment> {
    return this.apiService.post<Enrollment>(`enrollments/${enrollmentId}/complete`, {}).pipe(
      tap(completedEnrollment => {
        this.updateLocalEnrollment(completedEnrollment);
        
        // Generate certificate
        this.generateCompletionCertificate(enrollmentId);
        
        // Track course completion
        this.analyticsService.trackEvent(
          completedEnrollment.userId,
          'course_completed',
          { 
            enrollmentId, 
            courseId: completedEnrollment.courseId,
            completionTime: completedEnrollment.timeSpent,
            finalScore: this.calculateFinalScore(completedEnrollment)
          }
        ).subscribe();
        
        // Show completion celebration
        this.notificationService.showAchievementNotification({
          title: '🎉 Course Completed! 🎉',
          message: 'Congratulations! You\'ve successfully completed this course. Your certificate is being generated.'
        });
      }),
      catchError(error => {
        console.error('Failed to complete course:', error);
        return throwError(() => error);
      })
    );
  }

  // ENROLLMENT STATUS MANAGEMENT
  pauseEnrollment(enrollmentId: string, reason?: string): Observable<Enrollment> {
    return this.apiService.put<Enrollment>(`enrollments/${enrollmentId}/pause`, { reason }).pipe(
      tap(enrollment => {
        this.updateLocalEnrollment(enrollment);
      })
    );
  }

  resumeEnrollment(enrollmentId: string): Observable<Enrollment> {
    return this.apiService.put<Enrollment>(`enrollments/${enrollmentId}/resume`, {}).pipe(
      tap(enrollment => {
        this.updateLocalEnrollment(enrollment);
        
        // Send welcome back notification
        this.notificationService.showNotification({
          title: 'Welcome Back! 👋',
          message: 'Ready to continue your learning journey?',
          type: 'info'
        });
      })
    );
  }

  dropEnrollment(enrollmentId: string, reason?: string): Observable<void> {
    return this.apiService.put<void>(`enrollments/${enrollmentId}/drop`, { reason }).pipe(
      tap(() => {
        // Remove from local enrollments
        const currentEnrollments = this.enrollmentsSubject.value;
        const updatedEnrollments = currentEnrollments.filter(e => e.id !== enrollmentId);
        this.enrollmentsSubject.next(updatedEnrollments);
      })
    );
  }

  // ANALYTICS AND INSIGHTS
  getEnrollmentAnalytics(enrollmentId: string): Observable<any> {
    return this.apiService.get(`enrollments/${enrollmentId}/analytics`);
  }

  getStudyTime(enrollmentId: string, period: 'day' | 'week' | 'month' = 'week'): Observable<any> {
    return this.apiService.get(`enrollments/${enrollmentId}/study-time?period=${period}`);
  }

  getPerformanceInsights(enrollmentId: string): Observable<any> {
    return this.apiService.get(`enrollments/${enrollmentId}/insights`);
  }

  getRecommendedStudyPlan(enrollmentId: string): Observable<any> {
    return this.apiService.get(`enrollments/${enrollmentId}/study-plan`);
  }

  // BATCH OPERATIONS
  bulkUpdateProgress(updates: { enrollmentId: string; progress: Partial<EnrollmentProgress> }[]): Observable<Enrollment[]> {
    return this.apiService.post<Enrollment[]>('enrollments/bulk-update', { updates }).pipe(
      tap(updatedEnrollments => {
        updatedEnrollments.forEach(enrollment => {
          this.updateLocalEnrollment(enrollment);
        });
      })
    );
  }

  getUserActiveEnrollments(userId: string): Observable<Enrollment[]> {
    return this.getUserEnrollments(userId, { 
      status: ['active'], 
      includeCompleted: false,
      sortBy: 'lastAccessed'
    });
  }

  getUserCompletedEnrollments(userId: string): Observable<Enrollment[]> {
    return this.getUserEnrollments(userId, { 
      status: ['completed'], 
      sortBy: 'enrolledAt'
    });
  }

  // PROGRESS CALCULATIONS
  calculateOverallProgress(enrollment: Enrollment, course?: Course): number {
    if (!enrollment.progress) return 0;
    
    const totalLessons = this.getTotalLessonsCount(enrollment, course);
    const completedLessons = enrollment.progress.completedLessons.length;
    
    return totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;
  }

  calculateModuleProgress(enrollment: Enrollment, moduleId: string): number {
    const moduleProgress = enrollment.progress.completedLessons.filter(
      lessonId => this.isLessonInModule(lessonId, moduleId)
    ).length;
    
    const totalModuleLessons = this.getModuleLessonsCount(moduleId);
    
    return totalModuleLessons > 0 ? Math.round((moduleProgress / totalModuleLessons) * 100) : 0;
  }

  calculateTimeToCompletion(enrollment: Enrollment, course?: Course): { hours: number; days: number } {
    const totalDuration = course?.duration || 0; // in minutes
    const currentProgress = enrollment.progress.overallProgress;
    const remainingDuration = (totalDuration * (100 - currentProgress)) / 100;
    
    // Estimate based on user's average daily study time
    const avgDailyStudyTime = this.getAverageDailyStudyTime(enrollment.userId);
    
    return {
      hours: Math.ceil(remainingDuration / 60),
      days: Math.ceil(remainingDuration / avgDailyStudyTime)
    };
  }

  calculateStreakImpact(enrollment: Enrollment): Observable<any> {
    return this.apiService.get(`enrollments/${enrollment.id}/streak-impact`);
  }

  // STUDY SESSIONS
  startStudySession(enrollmentId: string, lessonId: string): Observable<string> {
    return this.apiService.post<{ sessionId: string }>(`enrollments/${enrollmentId}/study-session/start`, { lessonId }).pipe(
      map(response => response.sessionId),
      tap(sessionId => {
        // Store session in localStorage for recovery
        localStorage.setItem('activeStudySession', JSON.stringify({
          sessionId,
          enrollmentId,
          lessonId,
          startTime: new Date().toISOString()
        }));
      })
    );
  }

  endStudySession(sessionId: string, sessionData: {
    timeSpent: number;
    completionPercentage: number;
    interactions: any[];
    notes?: string;
  }): Observable<void> {
    return this.apiService.post<void>(`study-sessions/${sessionId}/end`, sessionData).pipe(
      tap(() => {
        // Clear active session
        localStorage.removeItem('activeStudySession');
      })
    );
  }

  getActiveStudySession(): any {
    const sessionStr = localStorage.getItem('activeStudySession');
    return sessionStr ? JSON.parse(sessionStr) : null;
  }

  // OFFLINE SUPPORT
  syncOfflineProgress(): Observable<any> {
    const offlineData = this.getOfflineProgressData();
    if (offlineData.length === 0) return new Observable(observer => observer.complete());

    return this.apiService.post('enrollments/sync-offline', { progressData: offlineData }).pipe(
      tap(() => {
        // Clear offline data after successful sync
        localStorage.removeItem('offlineProgress');
      }),
      catchError(error => {
        console.error('Failed to sync offline progress:', error);
        return throwError(() => error);
      })
    );
  }

  saveProgressOffline(enrollmentId: string, lessonId: string, progressData: any): void {
    const offlineData = this.getOfflineProgressData();
    offlineData.push({
      enrollmentId,
      lessonId,
      progressData,
      timestamp: new Date().toISOString()
    });
    
    localStorage.setItem('offlineProgress', JSON.stringify(offlineData));
  }

  private getOfflineProgressData(): any[] {
    const dataStr = localStorage.getItem('offlineProgress');
    return dataStr ? JSON.parse(dataStr) : [];
  }

  // HELPER METHODS
  private updateLocalEnrollment(updatedEnrollment: Enrollment): void {
    const enrollments = this.enrollmentsSubject.value;
    const index = enrollments.findIndex(e => e.id === updatedEnrollment.id);
    
    if (index !== -1) {
      enrollments[index] = updatedEnrollment;
      this.enrollmentsSubject.next([...enrollments]);
    }
    
    // Update current enrollment if it's the same
    const currentEnrollment = this.currentEnrollmentSubject.value;
    if (currentEnrollment?.id === updatedEnrollment.id) {
      this.currentEnrollmentSubject.next(updatedEnrollment);
    }
  }

  private checkModuleCompletion(enrollment: Enrollment, completedLessonId: string): void {
    // Logic to check if all lessons in a module are completed
    // This would typically query the course structure
    const moduleId = this.getModuleIdForLesson(completedLessonId);
    if (moduleId && this.isModuleCompleted(enrollment, moduleId)) {
      this.markModuleComplete(enrollment.id, moduleId).subscribe();
    }
  }

  private checkCourseCompletion(enrollment: Enrollment): void {
    // Check if all modules are completed
    if (this.areAllModulesCompleted(enrollment)) {
      this.completeCourse(enrollment.id).subscribe();
    }
  }

  private generateCompletionCertificate(enrollmentId: string): void {
    this.apiService.post(`enrollments/${enrollmentId}/certificate/generate`, {}).subscribe({
      next: (certificate) => {
        console.log('Certificate generated:', certificate);
      },
      error: (error) => {
        console.error('Failed to generate certificate:', error);
      }
    });
  }

  private sendEnrollmentNotification(enrollment: Enrollment): void {
    // This would typically integrate with your notification system
    console.log('Sending enrollment notification for:', enrollment.id);
  }

  private calculateFinalScore(enrollment: Enrollment): number {
    const quizScores = enrollment.progress.quizScores;
    if (quizScores.length === 0) return 0;
    
    const totalScore = quizScores.reduce((sum, quiz) => sum + (quiz.score / quiz.maxScore), 0);
    return Math.round((totalScore / quizScores.length) * 100);
  }

  private updateQuizScore(enrollmentId: string, quizScore: QuizScore): void {
    const enrollment = this.currentEnrollmentSubject.value;
    if (enrollment && enrollment.id === enrollmentId) {
      enrollment.progress.quizScores.push(quizScore);
      this.currentEnrollmentSubject.next(enrollment);
    }
  }

  private getTotalLessonsCount(enrollment: Enrollment, course?: Course): number {
    // This would calculate based on course structure
    // For now, return estimated count
    return course?.modules.reduce((total, module) => total + module.lessons.length, 0) || 20;
  }

  private getModuleLessonsCount(moduleId: string): number {
    // This would query course structure for module lesson count
    return 5; // Placeholder
  }

  private isLessonInModule(lessonId: string, moduleId: string): boolean {
    // This would check course structure to determine if lesson belongs to module
    return true; // Placeholder
  }

  private getModuleIdForLesson(lessonId: string): string | null {
    // This would query course structure to find module for lesson
    return 'module-1'; // Placeholder
  }

  private isModuleCompleted(enrollment: Enrollment, moduleId: string): boolean {
    return enrollment.progress.completedModules.includes(moduleId);
  }

  private areAllModulesCompleted(enrollment: Enrollment): boolean {
    // This would check against course structure
    const totalModules = 4; // Get from course data
    return enrollment.progress.completedModules.length >= totalModules;
  }

  private getAverageDailyStudyTime(userId: string): number {
    // This would calculate from user's historical data
    return 60; // 60 minutes average - placeholder
  }

  // PUBLIC API METHODS
  getEnrollmentStats(userId: string): Observable<any> {
    return this.apiService.get(`users/${userId}/enrollment-stats`).pipe(
      tap(stats => this.enrollmentStatsSubject.next(stats))
    );
  }

  searchUserEnrollments(userId: string, query: string): Observable<Enrollment[]> {
    return this.apiService.get<Enrollment[]>(`users/${userId}/enrollments/search?q=${encodeURIComponent(query)}`);
  }

  getUpcomingDeadlines(userId: string): Observable<any[]> {
    return this.apiService.get<any[]>(`users/${userId}/upcoming-deadlines`);
  }

  getRecentActivity(userId: string, limit: number = 10): Observable<any[]> {
    return this.apiService.get<any[]>(`users/${userId}/recent-activity?limit=${limit}`);
  }

  exportProgress(enrollmentId: string, format: 'pdf' | 'csv' | 'json' = 'pdf'): Observable<Blob> {
    return this.apiService.get<Blob>(`enrollments/${enrollmentId}/export?format=${format}`);
  }

  shareProgress(enrollmentId: string, platform: 'linkedin' | 'twitter' | 'facebook'): Observable<any> {
    return this.apiService.post(`enrollments/${enrollmentId}/share`, { platform });
  }
}
