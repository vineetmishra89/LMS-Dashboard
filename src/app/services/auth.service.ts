import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User } from '../models/user';
import { environment } from '../../environments/environment';

interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

interface AuthResponse {
  success: boolean;
  data: {
    user: User;
    token: string;
    refreshToken: string;
    expiresIn: number;
  };
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly tokenKey = 'authToken';
  private readonly refreshTokenKey = 'refreshToken';
  private readonly userKey = 'currentUser';
  
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  
  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const user = this.getCurrentUserFromStorage();
    
    if (token && user && !this.isTokenExpired(token)) {
      this.currentUserSubject.next(user);
      this.isAuthenticatedSubject.next(true);
    } else {
      this.logout();
    }
  }

  login(credentials: LoginCredentials): Observable<User> {
    return this.http.post<AuthResponse>(`${environment.authUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.success) {
          this.setAuthData(response.data);
          this.currentUserSubject.next(response.data.user);
          this.isAuthenticatedSubject.next(true);
        }
      }),
      map(response => response.data.user),
      catchError(error => {
        console.error('Login failed:', error);
        return throwError(() => error);
      })
    );
  }

  register(userData: RegisterData): Observable<User> {
    return this.http.post<AuthResponse>(`${environment.authUrl}/register`, userData).pipe(
      tap(response => {
        if (response.success) {
          this.setAuthData(response.data);
          this.currentUserSubject.next(response.data.user);
          this.isAuthenticatedSubject.next(true);
        }
      }),
      map(response => response.data.user),
      catchError(error => {
        console.error('Registration failed:', error);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.userKey);
    
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    
    this.router.navigate(['/login']);
  }

  refreshToken(): Observable<string> {
    const refreshToken = localStorage.getItem(this.refreshTokenKey);
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<AuthResponse>(`${environment.authUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        if (response.success) {
          this.setAuthData(response.data);
        }
      }),
      map(response => response.data.token),
      catchError(error => {
        console.error('Token refresh failed:', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${environment.authUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, password: string): Observable<any> {
    return this.http.post(`${environment.authUrl}/reset-password`, { token, password });
  }

  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    return this.http.post(`${environment.authUrl}/change-password`, {
      currentPassword,
      newPassword
    });
  }

  verifyEmail(token: string): Observable<any> {
    return this.http.post(`${environment.authUrl}/verify-email`, { token });
  }

  // Social Login
  googleLogin(): Observable<User> {
    // Implement Google OAuth
    return new Observable(); // Placeholder
  }

  facebookLogin(): Observable<User> {
    // Implement Facebook OAuth
    return new Observable(); // Placeholder
  }

  // Token Management
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private setAuthData(data: AuthResponse['data']): void {
    localStorage.setItem(this.tokenKey, data.token);
    localStorage.setItem(this.refreshTokenKey, data.refreshToken);
    localStorage.setItem(this.userKey, JSON.stringify(data.user));
    localStorage.setItem('userId', data.user.id);
  }

  private getCurrentUserFromStorage(): User | null {
    const userStr = localStorage.getItem(this.userKey);
    return userStr ? JSON.parse(userStr) : null;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000;
      return Date.now() >= exp;
    } catch {
      return true;
    }
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user ? user.role === role : false;
  }
}