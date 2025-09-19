

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
import { VideoProgressService } from '../../services/video-progress.service';
import { CourseDetail, CourseMaster } from '../../models/course';

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
  pendingFilters = { category: '', topic: '', instructor: '' };
  private appliedFilters$ = new BehaviorSubject<{
    category: string; topic: string; instructor: string;
  }>(this.pendingFilters);

  /** Track enrolled masters in-memory (mirror your real enrollment state if you have it) */
  private enrolledIds = new Set<string>();
  enrolledIds$ = new BehaviorSubject<Set<string>>(this.enrolledIds);

  /** Modal state */
  showDetails = false;
  selectedMaster: CourseMaster | null = null;

  displayedColumns = [
    'trainingName','description','topics','level','instructorName',
    'duration','category','prerequisite','toolsNeeded','reviewComments','action'
  ] as const;

  constructor(
    private router: Router,
    private userService: UserService,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private analyticsService: AnalyticsService,
    private certificateService: CertificateService,
    private notificationService: NotificationService,
    private videoProgressService: VideoProgressService
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

    const learningHours$ = this.videoProgressService.getLearningHours(userId).pipe(
      catchError(() => [{ totalHours: 0 }])
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

    this.dashboardData$ = combineLatest([analytics$, enrolled$, enrollments$, continue$, this.catalogCopy$, learningHours$]).pipe(
      map(([analytics, enrolled, enrollments, continueCourse, catalog, learningHours]: any) => ({
        stats: {
          completed: analytics.completedCount || 0,
          enrolled: analytics.enrolledCount || 0,
          hours: (learningHours && learningHours.totalHours) || 0
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
    this.catalog$ = this.appliedFilters$.pipe(
      // optional: debounce micro-changes if you type in a free-text filter
      debounceTime(0),
      //distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      switchMap(f => this.courseService.getAllCourses(f).pipe(catchError(() => [])).pipe(
        map(courses => {
          var out = courses;
          var category = this.pendingFilters.category;
          if (category && category !== '') {
            out = out.filter(function (c) { return c.category === category; });
          }
          var topic = this.pendingFilters.topic;
          if (topic && topic !== '') {
            out = out.filter(function (c) { return c.topics === topic; });
          }
          var instructor = this.pendingFilters.instructor;
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

  onContinueLearning(courseId: string, event?: Event): void {
    console.log('Continue button clicked, courseId:', courseId);
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    
    const courseData = this.latestVm?.continueCourse;
    if (courseData) {
      console.log('Navigating to video player with course data:', courseData);
      this.router.navigate(['/video-player', courseId], { 
        state: { courseData: courseData }
      }).then(
        success => console.log('Navigation success:', success),
        error => console.log('Navigation error:', error)
      );
    } else {
      console.log('No course data available, navigating without state');
      this.router.navigate(['/video-player', courseId]).then(
        success => console.log('Navigation success:', success),
        error => console.log('Navigation error:', error)
      );
    }
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


  onApplyFilters(): void {
  // take whatever the user picked and make it live
  this.appliedFilters$.next({ ...this.pendingFilters });
  // if your loadCourses() builds streams that depend on filters, you can call it here.
  // But with the combineLatest approach below, it's not required to rebuild anything.
}

onClearFilters(): void {
  this.pendingFilters = { category: '', topic: '', instructor: '' };
  this.appliedFilters$.next({ ...this.pendingFilters });
}

 trackByMaster = (_: number, m: CourseMaster) => m.trainingId;

  /** Enroll => mark as enrolled and (optionally) call backend */
  onEnroll(m: CourseMaster) {
    // this.enrollmentService.enroll(m.trainingId).subscribe(() => {
    this.enrolledIds.add(m.trainingId);
    this.enrolledIds$.next(new Set(this.enrolledIds));
    // });
  }

  /** Whether master is enrolled */
  isEnrolled(trainingId: string): boolean {
    return this.enrolledIds.has(trainingId);
  }

  /** Open modal */
  openDetails(m: CourseMaster) {
    this.selectedMaster = m;
    this.showDetails = true;
  }

  /** Close modal */
  closeDetails() {
    this.showDetails = false;
    this.selectedMaster = null;
  }

  /** Watch a detail video */
  onWatch(detail: CourseDetail) {
    if (!detail.trainingLink) return;
    // If you have a player route, navigate there instead:
    // this.router.navigate(['/player', detail.trainingId, detail.trainingDetailId]);
    console.log('opening video');
    window.open(detail.trainingLink, '_blank', 'noopener');
  }


}
