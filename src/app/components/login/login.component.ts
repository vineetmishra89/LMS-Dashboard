import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  returnUrl = '';
  showPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    const registered = this.route.snapshot.queryParams['registered'] === '1';
    if (registered) {
      this.errorMessage = '';
    }
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
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
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
