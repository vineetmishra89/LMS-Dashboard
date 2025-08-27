import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable, combineLatest, Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, map, startWith, catchError } from 'rxjs/operators';
import { User } from '../../models/user';
import { Course } from '../../models/course';
import { Enrollment } from '../../models/enrollment';
import { UserAnalytics } from '../../models/analytics';
import { Certificate } from '../../models/certificate';

import { UserService } from '../../services/user.service';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { AnalyticsService } from '../../services/analytics.service';
import { CertificateService } from '../../services/certificate.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  // Loading states
  isLoading$ = new BehaviorSubject<boolean>(true);
  hasError$ = new BehaviorSubject<string | null>(null);
  
  // User data
  currentUser$: Observable<User | null>;
  userStats$!: Observable<any>;
  
  // Course data
  enrolledCourses$!: Observable<Course[]>;
  trendingCourses$!: Observable<Course[]>;
  continuelearningCourse$!: Observable<Course | null>;
  
  // Progress data
  userAnalytics$!: Observable<UserAnalytics>;
  recentCertificates$!: Observable<Certificate[]>;
  currentStreak$!: Observable<number>;
  
  // Computed properties
  dashboardData$!: Observable<any>;

  constructor(
    private userService: UserService,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private analyticsService: AnalyticsService,
    private certificateService: CertificateService,
    private notificationService: NotificationService
  ) {
    this.currentUser$ = this.userService.currentUser$;
  }

  ngOnInit(): void {
    this.initializeDashboard();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeDashboard(): void {
    this.currentUser$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(user => {
      if (user) {
        this.loadUserDashboardData(user.id);
      }
    });
  }

  private loadUserDashboardData(userId: string): void {
    this.isLoading$.next(true);
    this.hasError$.next(null);

    // Load user statistics
    this.userStats$ = this.analyticsService.getUserStats(userId).pipe(
      catchError(error => {
        console.error('Failed to load user stats:', error);
        this.hasError$.next('Failed to load user statistics');
        return [];
      })
    );

    // Load enrolled courses
    this.enrolledCourses$ = this.courseService.getEnrolledCourses(userId).pipe(
      catchError(error => {
        console.error('Failed to load enrolled courses:', error);
        return [];
      })
    );

    // Load trending courses
    this.trendingCourses$ = this.courseService.getTrendingCourses(6).pipe(
      catchError(error => {
        console.error('Failed to load trending courses:', error);
        return [];
      })
    );

    // Get current learning course (most recently accessed)
    this.continuelearningCourse$ = combineLatest([
      this.enrollmentService.getUserEnrollments(userId),
      this.enrolledCourses$
    ]).pipe(
      map(([enrollments, courses]) => {
        const activeEnrollment = enrollments
          .filter(e => e.status === 'active' && e.progress.overallProgress < 100)
          .sort((a, b) => new Date(b.lastAccessedAt).getTime() - new Date(a.lastAccessedAt).getTime())[0];
        
        return activeEnrollment ? 
          courses.find(c => c.id === activeEnrollment.courseId) || null : 
          null;
      }),
      catchError(error => {
        console.error('Failed to load continue learning course:', error);
        return [null];
      })
    );

    // Load user analytics
    this.userAnalytics$ = this.analyticsService.getUserAnalytics(userId).pipe(
      catchError(error => {
        console.error('Failed to load user analytics:', error);
        return [{
          userId,
          totalCoursesEnrolled: 0,
          totalCoursesCompleted: 0,
          totalCertificatesEarned: 0,
          totalHoursLearned: 0,
          currentStreak: 0,
          longestStreak: 0,
          averageQuizScore: 0,
          skillsAcquired: [],
          learningPath: [],
          weeklyActivity: []
        }];
      })
    );

    // Load recent certificates
    this.recentCertificates$ = this.certificateService.getUserCertificates(userId).pipe(
      map(certificates => certificates
        .sort((a, b) => new Date(b.issuedAt).getTime() - new Date(a.issuedAt).getTime())
        .slice(0, 5)
      ),
      catchError(error => {
        console.error('Failed to load certificates:', error);
        return [];
      })
    );

    // Load learning streak
    this.currentStreak$ = this.analyticsService.getLearningStreak(userId).pipe(
      map(streak => streak.current),
      catchError(error => {
        console.error('Failed to load learning streak:', error);
        return [0];
      })
    );

    // Combine all data for the template
    this.dashboardData$ = combineLatest([
      this.userStats$,
      this.userAnalytics$,
      this.enrolledCourses$,
      this.trendingCourses$,
      this.continuelearningCourse$,
      this.recentCertificates$,
      this.currentStreak$
    ]).pipe(
      map(([stats, analytics, enrolledCourses, trendingCourses, continueCourse, certificates, streak]) => ({
        stats: {
          enrolledCourses: analytics.totalCoursesEnrolled,
          certificates: analytics.totalCertificatesEarned,
          hoursLearned: analytics.totalHoursLearned,
          usersOnline: stats.usersOnline || 2341 // Fallback for demo
        },
        analytics,
        enrolledCourses,
        trendingCourses,
        continueCourse,
        certificates,
        streak
      })),
      startWith(null),
      takeUntil(this.destroy$)
    );

    // Set loading to false after data loads
    this.dashboardData$.subscribe(data => {
      if (data) {
        this.isLoading$.next(false);
      }
    });
  }

  // Template helper methods
  onCourseEnroll(courseId: string): void {
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) {
      this.enrollmentService.enrollInCourse({ userId: currentUser.id, courseId }).subscribe({
        next: (enrollment) => {
          console.log('Successfully enrolled in course:', enrollment);
          // Refresh dashboard data
          this.loadUserDashboardData(currentUser.id);
        },
        error: (error) => {
          console.error('Failed to enroll in course:', error);
          this.hasError$.next('Failed to enroll in course. Please try again.');
        }
      });
    }
  }

  onContinueLearning(courseId: string): void {
    // Navigate to course player or course detail page
    // This would typically use Angular Router
    console.log('Continue learning course:', courseId);
  }

  onViewAllCourses(): void {
    // Navigate to courses page
    console.log('View all courses');
  }

  onViewAllCertificates(): void {
    // Navigate to certificates page
    console.log('View all certificates');
  }

  refreshDashboard(): void {
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) {
      this.loadUserDashboardData(currentUser.id);
    }
  }
}