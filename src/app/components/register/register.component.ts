import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', [Validators.required]],
      acceptTerms: [false, [Validators.requiredTrue]]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordValidator(control: any) {
    const value = control.value;
    if (!value) return null;
    
    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumbers = /\d/.test(value);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);
    
    const valid = hasUpperCase && hasLowerCase && hasNumbers && hasSpecialChar;
    
    if (!valid) {
      return { 
        passwordStrength: {
          hasUpperCase,
          hasLowerCase,
          hasNumbers,
          hasSpecialChar
        }
      };
    }
    
    return null;
  }

  private passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched();
      return;
    }
    
    this.isLoading = true;
    this.errorMessage = '';
    
    const userData = this.registerForm.value;
    delete userData.confirmPassword; // Don't send confirm password
    
    this.authService.register(userData).subscribe({
      next: (user) => {
        console.log('Registration successful:', user);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage = error.userMessage || 'Registration failed. Please try again.';
        this.isLoading = false;
      }
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.registerForm.controls).forEach(field => {
      const control = this.registerForm.get(field);
      control?.markAsTouched({ onlySelf: true });
    });
  }

  togglePasswordVisibility(field: 'password' | 'confirmPassword'): void {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  getPasswordStrengthClass(): string {
    const password = this.registerForm.get('password')?.value;
    if (!password) return '';
    
    const errors = this.registerForm.get('password')?.errors?.['passwordStrength'];
    if (!errors) return 'strong';
    
    const validCount = Object.values(errors).filter(v => v).length;
    if (validCount >= 3) return 'medium';
    if (validCount >= 2) return 'weak';
    return 'very-weak';
  }
}