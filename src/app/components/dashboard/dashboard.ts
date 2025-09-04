

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable, combineLatest, Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, map, startWith, catchError, switchMap, distinctUntilChanged, debounceTime, take, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User } from '../../models/user';
import { Enrollment } from '../../models/enrollment';
import { UserService } from '../../services/user.service';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { AnalyticsService } from '../../services/analytics.service';
import { CertificateService } from '../../services/certificate.service';
import { NotificationService } from '../../services/notification.service';
import { Course } from '../../models/course';

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
  private latestVm: any = null;
  catalog$!: Observable<any[]>;
  catalogCopy$!: Observable<any[]>;
  isFilterOperation: boolean = false;
  filters = { category: '', topic: '', instructor: '' };
  private filters$ = new BehaviorSubject<{
    category: string; topic: string; instructor: string;
  }>(this.filters);

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

    this.loadCourses();

    this.dashboardData$ = combineLatest([analytics$, enrolled$, enrollments$, continue$, this.catalogCopy$]).pipe(
      map(([analytics, enrolled, enrollments, continueCourse, catalog]: any) => ({
        stats: {
          completed: analytics.completedCount || 0,
          enrolled: analytics.enrolledCount || 0,
          hours: analytics.hoursLearned || 0
        },
        categories: [...new Set(catalog.map((c: any) => c.category))],
        topics: [...new Set(catalog.map((c: any) => c.topics))],
        instructors: [...new Set(catalog.map((c: any) => c.instructorName))],
        //catalog,
        enrollments,
        continueCourse
      })),
      startWith(null),
      takeUntil(this.destroy$)
    );

    this.dashboardData$.subscribe((d: any) => {
      if (d) {
        this.latestVm = d;
        this.isLoading$.next(false);
      }
    });
  }

  private loadCourses() {
    this.catalog$ = this.filters$.pipe(
      // optional: debounce micro-changes if you type in a free-text filter
      debounceTime(0),
      //distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      switchMap(f => this.courseService.getAllCourses(f).pipe(catchError(() => [])).pipe(
        map(courses => {
          var out = courses;
          var category = this.filters.category;
          if (category && category !== '') {
            out = out.filter(function (c) { return c.category === category; });
          }
          var topic = this.filters.topic;
          if (topic && topic !== '') {
            out = out.filter(function (c) { return c.topics === topic; });
          }
          var instructor = this.filters.instructor;
          if (instructor && instructor !== '') {
            out = out.filter(function (c) { return c.instructorName === instructor; });
          }
          console.log('Filtered Catalog courses loaded: ', out.length);
          return out;
        })
      ))  // <-- new HTTP per change
    );

    this.catalogCopy$ = this.catalog$.pipe(
      take(1),                                      // only first emission
      map(list => list.map(c => ({ ...c }))),       // clone
      shareReplay({ bufferSize: 1, refCount: true })// keep that first value forever
    );
  }

  onFilterChange<K extends 'category' | 'topic' | 'instructor'>(key: K, value: string) {
    console.log('Filter change', key, value);
    this.isFilterOperation = true;
    const next = { ...this.filters$.value, [key]: value ?? '' };
    this.filters$.next(next);
  }

  applyFilters(): void {
    this.loadCourses();
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

  getEnrollmentFor(courseId: string) {
    const vm = this.latestVm;
    return vm?.enrollments?.find((e: any) => e.courseId === courseId) || null;
  }

  private buildFilterParams(): any {
    const params: any = {};

    if (this.filters.category && this.filters.category !== '') {
      params.category = this.filters.category;
    }
    if (this.filters.topic && this.filters.topic !== '') {
      params.topic = this.filters.topic;
    }
    if (this.filters.instructor && this.filters.instructor !== '') {
      params.instructor = this.filters.instructor;
    }

    return params;
  }
}
