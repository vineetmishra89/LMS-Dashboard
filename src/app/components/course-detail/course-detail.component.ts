import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';
import { Course } from '../../models/course';
import { Enrollment } from '../../models/enrollment';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-course-detail',
  templateUrl: './course-detail.component.html',
  styleUrls: ['./course-detail.component.scss']
})
export class CourseDetailComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  course: Course | null = null;
  enrollment: Enrollment | null = null;
  isLoading = true;
  isEnrolling = false;
  activeTab = 'overview';
  reviews: any[] = [];
  relatedCourses: Course[] = [];
  showEnrollConfirm = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private userService: UserService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.params.pipe(
      switchMap(params => {
        const courseId = params['id'];
        this.isLoading = true;
        
        return combineLatest([
          this.courseService.getCourseById(courseId),
          this.loadUserEnrollment(courseId),
          this.courseService.getCourseReviews(courseId),
          this.loadRelatedCourses(courseId)
        ]);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: ([course, enrollment, reviews, relatedCourses]) => {
        this.course = course;
        this.enrollment = enrollment;
        this.reviews = reviews.reviews || [];
        this.relatedCourses = relatedCourses;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load course:', error);
        this.isLoading = false;
        this.router.navigate(['/courses']);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadUserEnrollment(courseId: string): Promise<Enrollment | null> {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return Promise.resolve(null);

    return this.enrollmentService.getUserEnrollments(currentUser.id).toPromise()
      .then(enrollments => enrollments?.find(e => e.courseId === courseId) || null)
      .catch(() => null);
  }

  private loadRelatedCourses(courseId: string): Promise<Course[]> {
    if (!this.course) return Promise.resolve([]);
    
    return this.courseService.getCoursesByCategory(this.course.category.id).toPromise()
      .then(courses => courses?.filter(c => c.id !== courseId).slice(0, 4) || [])
      .catch(() => []);
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  onEnrollClick(): void {
    if (this.course?.price > 0) {
      this.showEnrollConfirm = true;
    } else {
      this.enrollInCourse();
    }
  }

  enrollInCourse(): void {
    if (!this.course) return;
    
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: `/course/${this.course.id}` } });
      return;
    }

    this.isEnrolling = true;
    
    this.enrollmentService.enrollInCourse({
      userId: currentUser.id,
      courseId: this.course.id
    }).subscribe({
      next: (enrollment) => {
        this.enrollment = enrollment;
        this.isEnrolling = false;
        this.showEnrollConfirm = false;
        this.notificationService.showSuccessNotification(
          `Successfully enrolled in ${this.course?.title}!`,
          'Welcome to the Course! 🎉'
        );
      },
      error: (error) => {
        console.error('Enrollment failed:', error);
        this.notificationService.showErrorNotification('Enrollment failed. Please try again.');
        this.isEnrolling = false;
        this.showEnrollConfirm = false;
      }
    });
  }

  startLearning(): void {
    if (this.course && this.enrollment) {
      this.router.navigate(['/learn', this.course.id]);
    }
  }

  getProgressPercentage(): number {
    return this.enrollment?.progress.overallProgress || 0;
  }

  getCompletedLessons(): number {
    return this.enrollment?.progress.completedLessons.length || 0;
  }

  getTotalLessons(): number {
    return this.course?.modules.reduce((total, module) => total + module.lessons.length, 0) || 0;
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  }
}