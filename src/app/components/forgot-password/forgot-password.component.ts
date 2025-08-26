import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm!: FormGroup;
  isLoading = false;
  isSubmitted = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.forgotPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    const email = this.forgotPasswordForm.value.email;
    
    this.authService.forgotPassword(email).subscribe({
      next: (response) => {
        this.isSubmitted = true;
        this.successMessage = 'Password reset link has been sent to your email address.';
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.userMessage || 'Failed to send reset email. Please try again.';
        this.isLoading = false;
      }
    });
  }

  resendEmail(): void {
    if (this.forgotPasswordForm.valid) {
      this.onSubmit();
    }
  }
}