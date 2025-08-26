import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { User, UserPreferences } from '../models/user';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.loadCurrentUser();
  }

  private loadCurrentUser(): void {
    const userId = localStorage.getItem('userId');
    if (userId) {
      this.getUserById(userId).subscribe(
        user => this.currentUserSubject.next(user),
        error => console.error('Failed to load current user:', error)
      );
    }
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getUserById(userId: string): Observable<User> {
    return this.apiService.get<User>(`users/${userId}`);
  }

  updateUser(userId: string, userData: Partial<User>): Observable<User> {
    return this.apiService.put<User>(`users/${userId}`, userData);
  }

  updatePreferences(userId: string, preferences: UserPreferences): Observable<UserPreferences> {
    return this.apiService.put<UserPreferences>(`users/${userId}/preferences`, preferences);
  }

  uploadProfileImage(userId: string, imageFile: File): Observable<string> {
    const formData = new FormData();
    formData.append('image', imageFile);
    return this.apiService.post<string>(`users/${userId}/profile-image`, formData);
  }

  getUserStats(userId: string): Observable<any> {
    return this.apiService.get(`users/${userId}/stats`);
  }
}