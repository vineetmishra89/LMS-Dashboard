import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { FloatLabelModule } from 'primeng/floatlabel';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [InputTextModule, ButtonModule, FloatLabelModule ,CommonModule, FormsModule, ReactiveFormsModule ],
  standalone: true
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  returnUrl = '';
  showPassword = false;
  route =  inject(ActivatedRoute);

  constructor(
    private formBuilder: FormBuilder,
    public authService: AuthService,
    private router: Router
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
  }

  onSubmit(): void {
   // this.router.navigate(['/home']);
   // if (this.loginForm.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    
    const credentials = this.loginForm.value;
    this.authService.loginhardcode().subscribe({
      next: () => {
       // console.log('Login successful:', user);
        this.router.navigate(['/home']);
      }
    });
    
    // this.authService.login(credentials).subscribe({
    //   next: (user) => {
    //     console.log('Login successful:', user);
    //     this.router.navigate(['/home']);
    //   },
    //   error: (error) => {
    //     this.errorMessage = error.userMessage || 'Login failed. Please check your credentials.';
    //     this.isLoading = false;
    //   }
    // });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  
}
