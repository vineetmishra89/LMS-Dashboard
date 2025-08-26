import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1>Welcome Back</h1>
          <p>Sign in to continue your learning journey</p>
        </div>
        
        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="auth-form">
          <div class="form-group">
            <label for="email">Email</label>
            <input 
              id="email"
              type="email" 
              formControlName="email"
              class="form-control"
              [class.error]="loginForm.get('email')?.invalid && loginForm.get('email')?.touched">
            <div class="error-message" *ngIf="loginForm.get('email')?.invalid && loginForm.get('email')?.touched">
              Please enter a valid email address
            </div>
          </div>
          
          <div class="form-group">
            <label for="password">Password</label>
            <input 
              id="password"
              type="password" 
              formControlName="password"
              class="form-control"
              [class.error]="loginForm.get('password')?.invalid && loginForm.get('password')?.touched">
            <div class="error-message" *ngIf="loginForm.get('password')?.invalid && loginForm.get('password')?.touched">
              Password is required
            </div>
          </div>
          
          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" formControlName="rememberMe">
              <span class="checkmark"></span>
              Remember me
            </label>
            <a routerLink="/forgot-password" class="forgot-link">Forgot password?</a>
          </div>
          
          <button type="submit" class="btn btn-primary auth-btn" [disabled]="loginForm.invalid || isLoading">
            <span *ngIf="!isLoading">Sign In</span>
            <span *ngIf="isLoading" class="loading-text">
              <span class="spinner"></span>
              Signing In...
            </span>
          </button>
          
          <div class="error-display" *ngIf="errorMessage">
            {{ errorMessage }}
          </div>
        </form>
        
        <div class="social-login">
          <div class="divider">
            <span>or continue with</span>
          </div>
          <div class="social-buttons">
            <button class="btn btn-google" (click)="loginWithGoogle()" [disabled]="isLoading">
              🔍 Google
            </button>
            <button class="btn btn-facebook" (click)="loginWithFacebook()" [disabled]="isLoading">
              📘 Facebook
            </button>
          </div>
        </div>
        
        <div class="auth-footer">
          <p>Don't have an account? <a routerLink="/register">Sign up here</a></p>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./auth.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  returnUrl = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    
    const credentials = this.loginForm.value;
    
    this.authService.login(credentials).subscribe({
      next: (user) => {
        console.log('Login successful:', user);
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        this.errorMessage = error.userMessage || 'Login failed. Please check your credentials.';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  loginWithGoogle(): void {
    this.isLoading = true;
    this.authService.googleLogin().subscribe({
      next: (user) => {
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        this.errorMessage = 'Google login failed. Please try again.';
        this.isLoading = false;
      }
    });
  }

  loginWithFacebook(): void {
    this.isLoading = true;
    this.authService.facebookLogin().subscribe({
      next: (user) => {
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        this.errorMessage = 'Facebook login failed. Please try again.';
        this.isLoading = false;
      }
    });
  }
}