import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, Output, EventEmitter, AfterViewInit, inject } from '@angular/core';
import { interval, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { VideoProgressService } from '../../services/video-progress.service';
import { UserService } from '../../services/user.service';
import { CourseMaster } from '../../models/course';
import { EnrollmentDetails, EnrollmentMapping } from '../../models/enrollments';
import { ActivatedRoute } from '@angular/router';
import { CourseService } from '../../services/course.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.scss']
})
export class VideoPlayerComponent implements OnInit, OnDestroy, AfterViewInit   {
  @Input() videoUrl!: string;
  @Input() courseId!: number;
  @Input() lessonId!: number;
  @Input() selectedEnrollmentModule!: EnrollmentDetails;
  @Output() currentVideCompleted = new EventEmitter<boolean>();
  @Output() videoErrorChange = new EventEmitter<boolean>();
  @Output() showSignInPromptChange = new EventEmitter<boolean>();
  completed: boolean = false;
   route = inject(ActivatedRoute);
   courseService = inject(CourseService);
   messageService = inject(MessageService);
  
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
  
  showSignInPrompt = false;
  videoError = false;
  videoErrorMessage = '';
  
  constructor(
    private videoProgressService: VideoProgressService,
    private userService: UserService
  ) {}
  
  ngOnInit(): void {
    console.log('Load video progress');
    this.loadVideoProgress();
    this.setupProgressTracking();
    this.getCourseMaterial();
  }

  
ngAfterViewInit() {
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      this.videoElement.nativeElement.pause();
    }
  });
}

  
  ngOnDestroy(): void {
    this.saveCurrentProgress(false);
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
        this.saveCurrentProgress(false);
      }
    });
  }
  
  onVideoLoaded(): void {
    this.isLoading = false;
    this.duration = this.videoElement.nativeElement.duration;
    this.videoError = false;
    this.showSignInPrompt = false;
    this.videoErrorChange.emit(false);
    this.showSignInPromptChange.emit(false);
  }
  
  onVideoError(event: any): void {
    console.error('Video failed to load:', event);
    this.isLoading = false;
    this.videoError = true;
    
    this.videoErrorMessage = 'Unable to load video. You may need to sign in to Microsoft 365.';
    this.showSignInPrompt = true;
    
    this.videoErrorChange.emit(true);
    this.showSignInPromptChange.emit(true);
    
    const video = this.videoElement.nativeElement;
    if (video && video.error) {
      const errorCode = video.error.code;
      const errorMessage = video.error.message;
      console.error('Video error code:', errorCode, 'Message:', errorMessage);
    } else {
      console.error('Video error occurred but error details not available');
    }
  }
  
  openMicrosoftSignIn(): void {
    const sharePointUrl = 'https://irissoft-my.sharepoint.com';
    const signInWindow = window.open(sharePointUrl, 'Microsoft365SignIn', 'width=800,height=600');
    
    console.log('Opening Microsoft 365 sign-in window');
    
    this.showSignInPrompt = false;
    this.showSignInPromptChange.emit(false);
    
    this.videoErrorMessage = 'Please sign in to Microsoft 365 in the new window, then close it and click "Retry Video" below.';
  }
  
  retryVideo(): void {
    console.log('Retrying video playback');
    
    this.videoError = false;
    this.showSignInPrompt = false;
    this.videoErrorMessage = '';
    this.isLoading = true;
    
    this.videoErrorChange.emit(false);
    this.showSignInPromptChange.emit(false);
    
    const video = this.videoElement.nativeElement;
    video.load();
  }

  onVideoEnded(): void {
    console.log("video ended. Marking the module progress completed");
    this.onTimeUpdate();
    this.saveCurrentProgress(true);
    this.completed = false;
    this.currentVideCompleted.emit(true)
  }
  
  onPlay(): void {
    this.isPlaying = true;
    this.sessionStartTime = Date.now();
  }
  
  onPause(): void {
    console.log('Pausing the video');
    this.isPlaying = false;
    this.updateSessionWatchTime();
    this.saveCurrentProgress(false);
  }
  
  onTimeUpdate(): void {
    this.currentTime = this.videoElement.nativeElement.currentTime;
    this.progress = (this.currentTime / this.duration) * 100;
    console.log(this.currentTime, Math.floor(this.progress), this.duration);
    
  }
  
  private updateSessionWatchTime(): void {
    if (this.sessionStartTime > 0) {
      this.sessionWatchTime += (Date.now() - this.sessionStartTime) / 1000;
      this.sessionStartTime = 0;
    }
  }
  
  private saveCurrentProgress(videoCompleted: boolean): void {
    const user = this.userService.getCurrentUser();
    if (!user || !this.videoElement.nativeElement) return;
    
    this.updateSessionWatchTime();
    
    const watchTimeDelta = Math.max(0, this.sessionWatchTime);
    console.log("watchTimeDelta : "+watchTimeDelta);
    
    if (watchTimeDelta > 0 || videoCompleted) {
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

  getCourseMaterial() {
    const trainingId = this.route.snapshot.paramMap.get('trainingId') || '0';
    this.courseService.getCourseMaterial(trainingId).subscribe({
      next: (res) => {
        console.log('course material', res);
      }, error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error['error']['message'].split('from')[0] });
      }
    })
  }

  disableRightClick(event: MouseEvent) {
    event.preventDefault();
  }
}
