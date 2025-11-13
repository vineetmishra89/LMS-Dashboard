import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { interval, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { VideoProgressService } from '../../services/video-progress.service';
import { UserService } from '../../services/user.service';
import { CourseMaster } from '../../models/course';
import { EnrollmentDetails, EnrollmentMapping } from '../../models/enrollments';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.scss']
})
export class VideoPlayerComponent implements OnInit, OnDestroy {
  @Input() videoUrl!: string;
  @Input() courseId!: number;
  @Input() lessonId!: number;
  @Input() selectedEnrollmentModule!: EnrollmentDetails;
  completed: boolean = false;
  
  @ViewChild('videoElement', { static: true }) videoElement!: ElementRef<HTMLVideoElement>;
  
  private destroy$ = new Subject<void>();
  private progressTimer$ = interval(10000);
  private lastSavedTime = 0;
  private sessionWatchTime = 0;
  private sessionStartTime = 0;
  
  isLoading = true;
  isPlaying = false;
  currentTime = 0;
  duration = 0;
  progress = 0;
  
  constructor(
    private videoProgressService: VideoProgressService,
    private userService: UserService
  ) {}
  
  ngOnInit(): void {
    console.log('Load video progress');
    this.loadVideoProgress();
    this.setupProgressTracking();
  }
  
  ngOnDestroy(): void {
    this.saveCurrentProgress();
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  private loadVideoProgress(): void {
    this.videoProgressService.getProgress(this.selectedEnrollmentModule.enrollmentDetailsId)
      .subscribe({
        next: (progress) => {
          if (progress && this.videoElement.nativeElement) {
            this.videoElement.nativeElement.currentTime = progress.currentTime;
            this.lastSavedTime = progress.currentTime;
            console.log("current video progress : "+ progress.currentTime);
          }
        },
        error: (error) => {
          console.warn('Failed to load video progress:', error);
        }
      });
  }
  
  private setupProgressTracking(): void {
    this.progressTimer$.pipe(
      //takeUntil(this.destroy$)
    ).subscribe(() => {
      if (this.isPlaying) {
        this.saveCurrentProgress();
      }
    });
  }
  
  onVideoLoaded(): void {
    this.isLoading = false;
    this.duration = this.videoElement.nativeElement.duration;
  }

  onVideoEnded(): void {
    console.log("video ended. Marking the module progress completed");
    this.onTimeUpdate();
    this.saveCurrentProgress();
    this.completed = false;
  }
  
  onPlay(): void {
    this.isPlaying = true;
    this.sessionStartTime = Date.now();
  }
  
  onPause(): void {
    console.log('Pausing the video');
    this.isPlaying = false;
    this.updateSessionWatchTime();
    this.saveCurrentProgress();
  }
  
  onTimeUpdate(): void {
    this.currentTime = this.videoElement.nativeElement.currentTime;
    this.progress = (this.currentTime / this.duration) * 100;
  }
  
  private updateSessionWatchTime(): void {
    if (this.sessionStartTime > 0) {
      this.sessionWatchTime += (Date.now() - this.sessionStartTime) / 1000;
      this.sessionStartTime = 0;
    }
  }
  
  private saveCurrentProgress(): void {
    const user = this.userService.getCurrentUser();
    if (!user || !this.videoElement.nativeElement) return;
    
    this.updateSessionWatchTime();
    
    const watchTimeDelta = Math.max(0, this.sessionWatchTime);
    
    if (watchTimeDelta > 0) {
      console.log('Storing session watch time : '+ watchTimeDelta);
      if(this.currentTime >= this.duration) {
        this.completed = true;
      }
      this.videoProgressService.updateProgress({
        userId: user.id,
        courseId: this.courseId,
        lessonId: this.lessonId,
        currentTime: this.currentTime,
        duration: this.duration,
        watchTime: watchTimeDelta / 60,
        completed: this.completed,
        progress: (this.currentTime / this.duration) * 100,
        trainingEnrollmentDtlId:  this.selectedEnrollmentModule.enrollmentDetailsId
      }).subscribe({
        next: () => {
          console.log('Video progress saved successfully');
        },
        error: (error) => {
          console.warn('Failed to save video progress:', error);
        }
      });
      
      this.sessionWatchTime = 0;
    }
    
    this.lastSavedTime = this.currentTime;
  }
}
