import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface VideoProgressData {
  userId: string;
  courseId: string;
  lessonId: string;
  currentTime: number;
  duration: number;
  watchTime: number;
  completed: boolean;
  progress: number;
}

export interface VideoProgress {
  id: number;
  userId: string;
  courseId: string;
  lessonId: string;
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

  getProgress(userId: string, courseId: string, lessonId: string): Observable<VideoProgress> {
    return this.apiService.get<VideoProgress>(`video-progress/${userId}/${courseId}/${lessonId}`);
  }

  getLearningHours(userId: string): Observable<{ totalHours: number }> {
    return this.apiService.get<{ totalHours: number }>(`video-progress/learning-hours/${userId}`);
  }
}
