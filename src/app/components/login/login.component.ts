import { Component, inject, OnInit , Inject, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CommonModule,isPlatformBrowser } from '@angular/common';
import { FloatLabelModule } from 'primeng/floatlabel';
import { MSAuthService } from '../../services/msauth.service';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { Subject, takeUntil } from 'rxjs';
import { EventMessage, EventType, InteractionStatus } from '@azure/msal-browser';

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
  isIframe = false;
  private readonly _destroying$ = new Subject<void>();

  constructor(
    private formBuilder: FormBuilder,
    public authService: AuthService,
    private router: Router,
    private msAuthService: MSAuthService,
    private broadcastService: MsalBroadcastService,
    private msalService: MsalService,
    @Inject(PLATFORM_ID) private platformId: Object
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
      rememberMe: [false],
      ssologin:[]
    });

    if (isPlatformBrowser(this.platformId)) {
      this.isIframe = window !== window.parent && !window.opener;

      // Handle redirect response for non-iframe scenarios
      if (!this.isIframe) {
        this.msalService.handleRedirectObservable().subscribe({
          next: (result) => {
            if (result) {
              console.log('Redirect login successful', result);
              //this.setLoginDisplay();
              this.getAccessToken();
            }
          },
          error: (error) => console.error('Redirect login failed', error)
        });
      }

      // Initialize login display
    //  this.setLoginDisplay();

      this.broadcastService.inProgress$
        .pipe(takeUntil(this._destroying$))
        .subscribe((status: InteractionStatus) => {
          if (status === InteractionStatus.None) {
            //this.setLoginDisplay();
          }
        });
    }
  }

  login() {
    this.msAuthService.login().subscribe({
      next: (result) => {
        if (result) {
          console.log('Login successful', result);
         // this.setLoginDisplay();
          this.getAccessToken();
          this.router.navigate(['/home']);
        }
        // For redirect, result will be null and we'll handle success in handleRedirectObservable
      },
      error: (error) => console.error('Login failed', error)
    });
  }

  logout() {
    this.msAuthService.logout();
   // this.accessToken = null;
   // this.userInfo = null;
  }

  getAccessToken() {
    this.msAuthService.getAccessToken().subscribe({
      next: (result) => {
        //this.accessToken = result.accessToken;
        console.log('Access token:', result.accessToken);
        this.msAuthService.setToken(result.accessToken);

        // Validate token with backend and get files
        //this.validateTokenAndGetFiles();
        this.router.navigate(['/home']);
      },
      error: (error) => console.error('Failed to get access token', error)
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
