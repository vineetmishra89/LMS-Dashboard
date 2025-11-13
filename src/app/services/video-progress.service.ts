import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface VideoProgressData {
  userId: string;
  courseId: number;
  lessonId: number;
  currentTime: number;
  duration: number;
  watchTime: number;
  completed: boolean;
  progress: number;
  trainingEnrollmentDtlId:number;
}

export interface VideoProgress {
  id: number;
  userId: string;
  courseId: number;
  lessonId: number;
  currentTime: number;
  duration: number;
  watchTime: number;
  completed: boolean;
  lastWatchedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class VideoProgressService {
  constructor(private apiService: ApiService) {}

  updateProgress(data: VideoProgressData): Observable<VideoProgress> {
    return this.apiService.post<VideoProgress>('video-progress/update', data);
  }

  getProgress(enrollmentDetailsId: number): Observable<VideoProgress> {
    console.log('loading progress from backend')
    return this.apiService.get<VideoProgress>(`video-progress/${enrollmentDetailsId}`);
  }

  getLearningHours(userId: string): Observable<{ totalHours: number }> {
    return this.apiService.get<{ totalHours: number }>(`video-progress/learning-hours/${userId}`);
  }
}
