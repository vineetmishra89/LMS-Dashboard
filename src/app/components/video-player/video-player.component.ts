import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { interval, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { VideoProgressService } from '../../services/video-progress.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.scss']
})
export class VideoPlayerComponent implements OnInit, OnDestroy {
  @Input() videoUrl!: string;
  @Input() courseId!: string;
  @Input() lessonId: string = 'default';
  
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
    this.loadVideoProgress();
    this.setupProgressTracking();
  }
  
  ngOnDestroy(): void {
    this.saveCurrentProgress();
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  private loadVideoProgress(): void {
    const user = this.userService.getCurrentUser();
    if (!user) return;
    
    this.videoProgressService.getProgress(user.id, this.courseId, this.lessonId)
      .subscribe({
        next: (progress) => {
          if (progress && this.videoElement.nativeElement) {
            this.videoElement.nativeElement.currentTime = progress.currentTime;
            this.lastSavedTime = progress.currentTime;
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
  
  onPlay(): void {
    this.isPlaying = true;
    this.sessionStartTime = Date.now();
  }
  
  onPause(): void {
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
      this.videoProgressService.updateProgress({
        userId: user.id,
        courseId: this.courseId,
        lessonId: this.lessonId,
        currentTime: this.currentTime,
        duration: this.duration,
        watchTime: watchTimeDelta / 60
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
