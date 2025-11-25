
// src/app/app.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { AuthService } from './services/auth.service';
import { ConfigService } from './services/config.service';
import { LoadingInterceptor } from './interceptors/loading.interceptor';
import { PrimeNGConfig } from 'primeng/api';
import { RoleService } from './services/role.service';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  title = 'LMS Dashboard';
  isAuthenticated = false;
  isLoading = false;
  currentUser: any = null;
  syncStatus: any = null;
  
  // Global UI state
  showMobileMenu = false; 
  isDarkMode = false;
  showSyncIndicator = false;
  showHeader = false;
  showLndAdmin = false;
  showRODashboard = false;

public showInstallPrompt = false;
private deferredPrompt: any = null;

  constructor(
    private authService: AuthService,
  //  private notificationService: NotificationService,
    //private dataSyncService: DataSyncService,
    private configService: ConfigService,
    private loadingInterceptor: LoadingInterceptor,
    private router: Router,
    private primengConfig: PrimeNGConfig,
    private roleService: RoleService
  ) {
    this.initializeApp();
  }

  ngOnInit(): void {
    this.primengConfig.ripple = true;
    
    this.setupAuthenticationListener();
    
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: any) => {
      this.updateHeaderVisibility(event.urlAfterRedirects || event.url);
    });
    
    this.authService.isAuthenticated$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.updateHeaderVisibility(this.router.url);
    });
    
    this.updateHeaderVisibility(this.router.url);
    
    if (this.authService.isLoggedIn() && this.roleService.getCurrentRoles().length === 0) {
      console.log('User already authenticated on app init, fetching roles...');
      this.loadUserRoles();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeApp(): void {
    // Initialize monitoring
    //this.monitoringService.trackComponentLoad('AppComponent');
    
    // Load user preferences
    //this.loadUserPreferences();
    
    // Initialize PWA features
    //this.initializePWA();
  }

  private setupAuthenticationListener(): void {
    this.authService.isAuthenticated$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(isAuthenticated => {
      this.isAuthenticated = isAuthenticated;
      if (!isAuthenticated) {
        this.roleService.clearRoles();
        this.showLndAdmin = false;
        this.showRODashboard = false;
      } else {
        if (this.currentUser && this.roleService.getCurrentRoles().length === 0) {
          this.loadUserRoles();
        }
      }
    });

    this.authService.currentUser$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadUserPreferences();
        this.loadUserRoles();
      }
    });

    this.roleService.roles$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.updateRoleVisibility();
    });
  }

  private updateHeaderVisibility(url: string): void {
    const authRoutes = ['/login', '/forgot-password'];
    const isAuthRoute = authRoutes.some(route => url?.startsWith(route));
    
    this.showHeader = this.isAuthenticated && !isAuthRoute;
  }

  private loadUserRoles(): void {
    this.roleService.fetchUserRoles().subscribe({
      next: () => {
        this.updateRoleVisibility();
      },
      error: (error) => {
        console.error('Error loading user roles:', error);
        this.showLndAdmin = false;
        this.showRODashboard = false;
      }
    });
  }

  private updateRoleVisibility(): void {
    this.showLndAdmin = this.roleService.isLndAdmin();
    this.showRODashboard = this.roleService.canAccessRODashboard();
  }

  private loadUserPreferences(): void {
    if (this.currentUser?.preferences) {
      this.isDarkMode = this.currentUser.preferences.theme === 'dark';
      this.applyTheme();
    }
  }

  private applyTheme(): void {
    const body = document.body;
    if (this.isDarkMode) {
      body.classList.add('dark-theme');
    } else {
      body.classList.remove('dark-theme');
    }
  }

  // private initializePWA(): void {
  //   // Service worker update check
  //   if ('serviceWorker' in navigator) {
  //     navigator.serviceWorker.addEventListener('controllerchange', () => {
  //       this.notificationService.showNotification({
  //         title: 'App Updated',
  //         message: 'The app has been updated to the latest version.',
  //         type: 'info',
  //         action: {
  //           text: 'Reload',
  //           callback: () => window.location.reload()
  //         }
  //       });
  //     });
  //   }

    // Install prompt handling
  //   window.addEventListener('beforeinstallprompt', (e) => {
  //     e.preventDefault();
      
  //     this.notificationService.showNotification({
  //       title: 'Install App',
  //       message: 'Install our app for a better learning experience!',
  //       type: 'info',
  //       action: {
  //         text: 'Install',
  //         callback: () => {
  //           (e as any).prompt();
  //         }
  //       },
  //       duration: 10000
  //     });
  //   });
  // }

  // Template event handlers
  toggleMobileMenu(): void {
    this.showMobileMenu = !this.showMobileMenu;
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    
    // Save preference
    if (this.currentUser) {
      const preferences = {
        ...this.currentUser.preferences,
        theme: this.isDarkMode ? 'dark' : 'light'
      };
      
      // Update user preferences (you'd call userService here)
      console.log('Theme updated to:', preferences.theme);
    }
  }

  // onSyncRetry(): void {
  //   this.dataSyncService.forceSyncAll().subscribe({
  //     next: () => {
  //       this.notificationService.showSuccessNotification('Sync completed successfully!');
  //     },
  //     error: () => {
  //       this.notificationService.showErrorNotification('Sync failed. Please try again later.');
  //     }
  //   });
  // }

  logout(): void {
    this.roleService.clearRoles();
    this.authService.logout();
  }

  installPWA(): void {
  if (this.deferredPrompt) {
    this.deferredPrompt.prompt();
    this.deferredPrompt.userChoice.then((choiceResult: any) => {
      if (choiceResult.outcome === 'accepted') {
        console.log('User accepted the install prompt');
      } else {
        console.log('User dismissed the install prompt');
      }
      this.deferredPrompt = null;
      this.showInstallPrompt = false;
    });
  }
}

dismissInstallPrompt(): void {
  this.showInstallPrompt = false;
  this.deferredPrompt = null;
}


}
