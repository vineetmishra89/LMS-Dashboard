import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { FloatLabelModule } from 'primeng/floatlabel';
import { Globals } from '../shared/globals';
import { msalInstance } from '../../auth.config';
import { MsalService } from '@azure/msal-angular';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [InputTextModule, ButtonModule, FloatLabelModule, CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  standalone: true
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  returnUrl = '';
  showPassword = false;
  route =  inject(ActivatedRoute);
  globals = inject(Globals);
  accessToken: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    public authService: AuthService,
    private router: Router,
    private msalService: MsalService
  ) {}

  ngOnInit(): void {
    const x = this.authService.isLoggedIn();
    console.log(x);
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
    const registered = this.route.snapshot.queryParams['registered'] === '1';
    if (registered) {
      this.errorMessage = '';
    }
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });

    // 1️⃣ Process the redirect result if we just came back from loginRedirect()
    this.msalService.instance.handleRedirectPromise().then(result => {
      if (result && result.account) {
        console.log('MSAL redirect result, setting active account:', result.account);
        msalInstance.setActiveAccount(result.account);
      }
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      return;
    }
    
    this.isLoading = true;
    this.errorMessage = '';
    
    const credentials = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };
    
    this.authService.login(credentials).subscribe({
      next: (response) => {

        console.log('Login successful:', response);

        this.getAccessToken();
        this.isLoading = false;
        
        this.globals.setUser(JSON.stringify(response));
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        this.errorMessage = error.userMessage || 'Login failed. Please check your credentials.';
        this.isLoading = false;
      }
    });
  }

    getAccessToken() {
      this.authService.getAccessToken().subscribe({
        next: (result) => {
          this.accessToken = result.accessToken;
          console.log('Access token:', this.accessToken);
  
          // Validate token with backend and get files
          this.validateToken();
          sessionStorage.setItem('accessToken', this.accessToken);
        },
        error: (error) => console.error('Failed to get access token', error)
      });
    }

    validateToken() {
      if (this.accessToken) {
        // Validate token with Spring Boot backend
        this.authService.validateTokenWithBackend(this.accessToken).subscribe({
          next: (response) => {
            console.log('Token validation response:', response);
          },
          error: (error) => console.error('Token validation failed:', error)
        });
      }
    }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  
}
