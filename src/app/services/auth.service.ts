import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
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
    
    if (token && user) {
      if (this.isTokenExpired(token)) {
        this.clearAuthStorage();
        this.isAuthenticatedSubject.next(false);
        return;
      }
      
      const tokenType = sessionStorage.getItem('tokenType');
      const expiresAt = sessionStorage.getItem('expiresAt');
      
      if (tokenType && expiresAt) {
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
        return;
      }
      
      this.clearAuthStorage();
      this.isAuthenticatedSubject.next(false);
      return;
    }

  if (environment.devAutoLogin) {
    const exp = Math.floor(Date.now()/1000) + 60*60*24*365;
    const payload = btoa(JSON.stringify({ exp }));
    localStorage.setItem(this.tokenKey, `x.${payload}.y`);

    const demo = { id: 'demo-user', name: 'Vineet Mishra', email: 'vineet@example.com', role: 'student' } as any;
    localStorage.setItem(this.userKey, JSON.stringify(demo));
    localStorage.setItem('userId', demo.id);

    this.currentUserSubject.next(demo);
    this.isAuthenticatedSubject.next(true);
    return;
  }

  // default: not authenticated
  this.isAuthenticatedSubject.next(false);
  }

  private clearAuthStorage(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem('tokenType');
    sessionStorage.removeItem('expiresAt');
    sessionStorage.removeItem(this.userKey);
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem('userId');
  }

  loginhardcode() {
     this.isAuthenticatedSubject.next(true);
     return of(null);
  }

  login(credentials: LoginCredentials): Observable<User> {
    const loginRequest = { 
      emailId: credentials.email, 
      password: credentials.password 
    };
    
    return this.http.post<any>(`${environment.apiUrl}/auth/login`, loginRequest).pipe(
      tap(response => {
        if (response.token) {
          sessionStorage.setItem(this.tokenKey, response.token);
          sessionStorage.setItem('tokenType', response.tokenType);
          sessionStorage.setItem('expiresAt', response.expiresAt);
          sessionStorage.setItem(this.userKey, JSON.stringify(response.user));
          localStorage.setItem('userId', response.user.emailId);
          
          this.currentUserSubject.next(response.user as any);
          this.isAuthenticatedSubject.next(true);
          
          console.log('Login successful:', response.user);
        }
      }),
      map(response => response.user as any),
      catchError(error => {
        console.error('Login failed:', error);
        const errorMessage = error.error?.message || 'Login failed. Please check your credentials.';
        return throwError(() => ({ userMessage: errorMessage, error }));
      })
    );
  }

  register(userData: RegisterData): Observable<User | null> {
    return this.http.post<AuthResponse>(`${environment.authUrl}/register`, userData).pipe(
      tap(response => {
        if (response.success && response.data) {
          if (response.data.token) {
            this.setAuthData(response.data);
            this.currentUserSubject.next(response.data.user);
            this.isAuthenticatedSubject.next(true);
          }
        }
      }),
      map(response => response.data?.user ?? null),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    const token = this.getToken();
    
    if (token) {
      this.http.post(`${environment.apiUrl}/auth/logout`, {}).subscribe({
        next: () => console.log('Logout successful'),
        error: (error) => console.error('Logout error:', error)
      });
    }
    
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem('tokenType');
    sessionStorage.removeItem('expiresAt');
    sessionStorage.removeItem(this.userKey);
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem('userId');
    
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
    return this.http.post(`${environment.apiUrl}/auth/change-password`, {
      currentPassword,
      newPassword
    }).pipe(
      tap(response => {
        console.log('Password changed successfully');
      }),
      catchError(error => {
        console.error('Password change error:', error);
        const errorMessage = error.error?.message || 'Password change failed.';
        return throwError(() => ({ userMessage: errorMessage, error }));
      })
    );
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
    return sessionStorage.getItem(this.tokenKey) || localStorage.getItem(this.tokenKey);
  }

  private setAuthData(data: AuthResponse['data']): void {
    localStorage.setItem(this.tokenKey, data.token);
    localStorage.setItem(this.refreshTokenKey, data.refreshToken);
    localStorage.setItem(this.userKey, JSON.stringify(data.user));
    localStorage.setItem('userId', data.user.id);
  }

  private getCurrentUserFromStorage(): User | null {
    const userStr = sessionStorage.getItem(this.userKey) || localStorage.getItem(this.userKey);
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
