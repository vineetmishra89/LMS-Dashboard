import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable, combineLatest, Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, map, startWith, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User } from '../../models/user';
import { Enrollment } from '../../models/enrollment';
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
  isLoading$ = new BehaviorSubject<boolean>(true);
  hasError$ = new BehaviorSubject<string | null>(null);

  currentUser$: Observable<User | null>;
  dashboardData$!: Observable<any>;

  filters = { category: '', topic: '', instructor: '' };

  constructor(
    private router: Router,
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
    this.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((u: User | null) => {
      if (u) this.loadUserDashboardData(u.id);
    });
  }

  private loadUserDashboardData(userId: string): void {
    this.isLoading$.next(true);
    this.hasError$.next(null);

    const analytics$ = this.analyticsService.getUserAnalytics(userId).pipe(
      catchError(() => [{
        userId,
        totalCoursesEnrolled: 0,
        totalCoursesCompleted: 0,
        totalCertificatesEarned: 0,
        totalHoursLearned: 0
      } as any])
    );

    const enrolled$ = this.courseService.getEnrolledCourses(userId).pipe(catchError(() => []));
    const enrollments$ = this.enrollmentService.getUserEnrollments(userId).pipe(catchError(() => []));
    const continue$ = combineLatest([enrollments$, enrolled$]).pipe(
      map(([enrollments, courses]: any) => {
        const active = enrollments
          .filter((e: any) => e.status === 'active' && e.progress.overallProgress < 100)
          .sort((a: any, b: any) => new Date(b.lastAccessedAt).getTime() - new Date(a.lastAccessedAt).getTime())[0];
        return active ? courses.find((c: any) => c.id === active.courseId) || null : null;
      }),
      catchError(() => [null])
    );

    const catalog$ = this.courseService.getAllCourses(this.filters as any).pipe(catchError(() => []));

    this.dashboardData$ = combineLatest([analytics$, enrolled$, enrollments$, continue$, catalog$]).pipe(
      map(([analytics, enrolled, enrollments, continueCourse, catalog]: any) => ({
        stats: {
          completed: analytics.totalCoursesCompleted || 0,
          enrolled: analytics.totalCoursesEnrolled || 0,
          hours: analytics.totalHoursLearned || 0
        },
        categories: [...new Set(catalog.map((c: any) => c.category))],
        topics: [...new Set(catalog.flatMap((c: any) => c.topics || []))],
        instructors: [...new Set(catalog.map((c: any) => (c.instructor?.name || c.instructor)))],
        catalog,
        enrollments,
        continueCourse
      })),
      startWith(null),
      takeUntil(this.destroy$)
    );

    this.dashboardData$.subscribe((d: any) => d && this.isLoading$.next(false));
  }

  applyFilters(): void {
    const u = this.userService.getCurrentUser();
    if (u) this.loadUserDashboardData(u.id);
  }

  onCourseEnroll(courseId: string): void {
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) {
      this.enrollmentService.enrollInCourse({ userId: currentUser.id, courseId }).subscribe({
        next: () => this.loadUserDashboardData(currentUser.id),
        error: () => this.hasError$.next('Failed to enroll in course. Please try again.')
      });
    }
  }

  onContinueLearning(courseId: string): void {
    this.router.navigate(['/course', courseId]);
  }

  openCompleted(): void { this.router.navigate(['/detail/completed']); }
  openEnrolled(): void { this.router.navigate(['/detail/enrolled']); }
  openHours(): void { this.router.navigate(['/detail/hours']); }

  refreshDashboard(): void {
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) this.loadUserDashboardData(currentUser.id);
  }
}
