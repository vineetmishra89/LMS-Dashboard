import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Course } from '../../models/course';
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
  @Input() course!: Course;
  @Input() enrollment?: Enrollment;
  @Input() showEnrollButton: boolean = false;
  @Input() showProgress: boolean = true;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() userId!: string;
  
  @Output() enrollClick = new EventEmitter<string>();
  @Output() continueClick = new EventEmitter<string>();
  @Output() viewDetails = new EventEmitter<string>();

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
    if (this.course.durationMinutes && this.enrollment) {
      const completedTime = (this.course.durationMinutes * this.progressPercentage) / 100;
      const remaining = this.course.durationMinutes - completedTime;
      this.timeRemaining = this.formatDuration(remaining);
    } else if (this.course.durationMinutes) {
      this.timeRemaining = this.formatDuration(this.course.durationMinutes);
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
      this.enrollClick.emit(this.course.id);
    }
  }

  onContinue(): void {
    this.continueClick.emit(this.course.id);
    if (this.course.videoUrl) {
      this.onWatchVideo();
    }
  }

  onViewDetails(): void {
    this.viewDetails.emit(this.course.id);
  }

  onWatchVideo(): void {
    this.continueClick.emit(this.course.id);
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
