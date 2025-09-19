import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CourseDetail, CourseMaster } from '../../models/course';
import { Enrollment } from '../../models/enrollment';
import { EnrollmentService } from '../../services/enrollment.service';
import { CourseService } from '../../services/course.service';
import { AnalyticsService } from '../../services/analytics.service';

type ChatMessage = { id: string; message?: string };

@Component({
  selector: 'app-course-card',
  templateUrl: './course-card.html',
  styleUrls: ['./course-card.scss']
})
export class CourseCardComponent implements OnInit {
  @Input() course!: CourseMaster;
  @Input() enrollment?: Enrollment;
  @Input() showEnrollButton: boolean = false;
  @Input() showProgress: boolean = true;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() userId!: string;
  courseDetail!: CourseDetail;
  
  @Output() enrollClick = new EventEmitter<string>();
  @Output() continueClick = new EventEmitter<string>();
  @Output() viewDetails = new EventEmitter<string>();

  @Input() master!: CourseMaster;
  @Input() enrolled = false;
  @Output() enroll = new EventEmitter<CourseMaster>();
  @Output() view = new EventEmitter<CourseMaster>();

  onEnrollClick(){ this.enroll.emit(this.master); }
  onViewClick(){ this.view.emit(this.master); }

  isEnrolling: boolean = false;
  progressPercentage: number = 0;
  timeRemaining: string = '';
  isTyping: boolean = false;
  typingSubject: any;
  messagesContainer: any;
  messages: ChatMessage[] = [];

  constructor(private enrollmentService: EnrollmentService,
    private courseService: CourseService,private analyticsService: AnalyticsService
  ) {}

  ngOnInit(): void {
    this.calculateProgress();
    this.calculateTimeRemaining();
  }

  private calculateProgress(): void {
    if (this.enrollment?.progress?.overallProgress !== undefined) {
      this.progressPercentage = this.enrollment.progress.overallProgress;
    } else {
      this.progressPercentage = 0;
    }
  }

  private calculateTimeRemaining(): void {
    if (this.course.duration && this.enrollment) {
      const completedTime = (this.course.duration * this.progressPercentage) / 100;
      const remaining = this.course.duration - completedTime;
      this.timeRemaining = this.formatDuration(remaining);
    } else if (this.course.duration) {
      this.timeRemaining = this.formatDuration(this.course.duration);
    }
  }

  private formatDuration(minutes: number): string {
    const minute = Math.floor(minutes / 60);
    const sec = Math.floor(minutes % 60);
    
    if (minute > 0) {
      return `${minute}m ${sec}s`;
    }
    return `${sec}m`;
  }

  onEnroll(): void {
    if (!this.isEnrolling) {
      this.isEnrolling = true;
      this.enrollClick.emit(this.course.trainingId);
    }
  }

  onContinue(): void {
    this.continueClick.emit(this.courseDetail.trainingDetailId);
    if (this.courseDetail.trainingLink) {
      this.onWatchVideo();
    }
  }

  onViewDetails(): void {
    this.viewDetails.emit(this.course.trainingId);
  }

  onWatchVideo(): void {
    this.continueClick.emit(this.courseDetail.trainingDetailId);
  }

    getStatusText(): string {
    if (!this.enrollment) return 'Not Enrolled';
    
    switch (this.enrollment.status) {
      case 'active':
        return this.progressPercentage === 100 ? 'Completed' : 'In Progress';
      case 'completed':
        return 'Completed';
      case 'paused':
        return 'Paused';
      case 'dropped':
        return 'Dropped';
      default:
        return 'Unknown';
    }
  }

    getProgressColor(): string {
    if (this.progressPercentage >= 90) return '#10b981'; // green
    if (this.progressPercentage >= 60) return '#3b82f6'; // blue
    if (this.progressPercentage >= 30) return '#f59e0b'; // yellow
    return '#ef4444'; // red
  }
}
