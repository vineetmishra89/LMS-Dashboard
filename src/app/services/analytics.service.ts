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
    return this.apiService.get<UserAnalytics>(`analytics/summary?userId=${userId}`);
  }

  getUserStats(userId: string): Observable<any> {
    return this.apiService.get(`analytics/stats?userId=${userId}`);
  }

  getWeeklyActivity(userId: string, weeks: number = 12): Observable<WeeklyActivity[]> {
    return this.apiService.get<WeeklyActivity[]>(`analytics/weekly-activity?userId=${userId}&weeks=${weeks}`);
  }

  getLearningStreak(userId: string): Observable<{ current: number; longest: number }> {
    return this.apiService.get(`analytics/learning-streak?userId=${userId}`);
  }

  getSkillsProgress(userId: string): Observable<any> {
    return this.apiService.get(`analytics/skills-progress?userId=${userId}`);
  }

  getCourseCompletionRate(userId: string): Observable<number> {
    return this.apiService.get<number>(`analytics/completion-rate?userId=${userId}`);
  }

  getRecommendations(userId: string): Observable<any> {
    return this.apiService.get(`analytics/recommendations?userId=${userId}`);
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
