import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RoleService } from '../../services/role.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { FloatLabelModule } from 'primeng/floatlabel';
import { Globals } from '../../core/globals';

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

  constructor(
    private formBuilder: FormBuilder,
    public authService: AuthService,
    private router: Router,
    private roleService: RoleService
  ) {}

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
    
    if (this.authService.isLoggedIn()) {
      console.log('User already authenticated, redirecting to:', this.returnUrl);
      this.router.navigate([this.returnUrl]);
      return;
    }
    
    const x = this.authService.isLoggedIn();
    console.log(x);
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
        this.isLoading = false;
        
        this.globals.setUser(JSON.stringify(response));
        
        this.roleService.clearRoles();
        this.roleService.fetchUserRoles().subscribe({
          next: (roles) => {
            console.log('Roles fetched after login:', roles);
            this.router.navigate([this.returnUrl]);
          },
          error: (roleError) => {
            console.error('Error fetching roles after login:', roleError);
            this.router.navigate([this.returnUrl]);
          }
        });
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

  
}
