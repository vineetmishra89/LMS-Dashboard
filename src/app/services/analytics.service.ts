import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserAnalytics, WeeklyActivity } from '../models/analytics';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  constructor(private apiService: ApiService) {}

  getUserAnalytics(userId: string): Observable<UserAnalytics> {
    return this.apiService.get<UserAnalytics>(`users/${userId}/analytics`);
  }

  getUserStats(userId: string): Observable<any> {
    return this.apiService.get(`users/${userId}/stats`);
  }

  getWeeklyActivity(userId: string, weeks: number = 12): Observable<WeeklyActivity[]> {
    return this.apiService.get<WeeklyActivity[]>(`users/${userId}/weekly-activity?weeks=${weeks}`);
  }

  getLearningStreak(userId: string): Observable<{ current: number; longest: number }> {
    return this.apiService.get(`users/${userId}/learning-streak`);
  }

  getSkillsProgress(userId: string): Observable<any> {
    return this.apiService.get(`users/${userId}/skills-progress`);
  }

  getCourseCompletionRate(userId: string): Observable<number> {
    return this.apiService.get<number>(`users/${userId}/completion-rate`);
  }

  getRecommendations(userId: string): Observable<any> {
    return this.apiService.get(`users/${userId}/recommendations`);
  }

  trackEvent(userId: string, eventType: string, eventData: any): Observable<void> {
    return this.apiService.post('analytics/events', {
      userId,
      eventType,
      eventData,
      timestamp: new Date().toISOString()
    });
  }
}