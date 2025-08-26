
// src/app/app.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { AuthService } from './services/auth.service';
import { NotificationService } from './services/notification.service';
import { DataSyncService } from './services/data-sync.service';
import { WebSocketService } from './services/websocket.service';
import { MonitoringService } from './services/monitoring.service';
import { ConfigService } from './services/config.service';
import { LoadingInterceptor } from './interceptors/loading.interceptor';

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

  private showInstallPrompt = false;
private deferredPrompt: any = null;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private dataSyncService: DataSyncService,
    private webSocketService: WebSocketService,
    private monitoringService: MonitoringService,
    private configService: ConfigService,
    private loadingInterceptor: LoadingInterceptor,
    private router: Router
  ) {
    this.initializeApp();
  }

  ngOnInit(): void {
    this.setupAuthenticationListener();
    this.setupRouteTracking();
    this.setupSyncStatusListener();
    this.setupLoadingIndicator();
    this.setupErrorHandling();
    this.initializeNotifications();

     // PWA install prompt handling
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    this.deferredPrompt = e;
    this.showInstallPrompt = true;
  });

  window.addEventListener('appinstalled', () => {
    this.showInstallPrompt = false;
    this.deferredPrompt = null;
    this.notificationService.showSuccessNotification('App installed successfully!');
  });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeApp(): void {
    // Initialize monitoring
    this.monitoringService.trackComponentLoad('AppComponent');
    
    // Load user preferences
    this.loadUserPreferences();
    
    // Initialize PWA features
    this.initializePWA();
  }

  private setupAuthenticationListener(): void {
    this.authService.isAuthenticated$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(isAuthenticated => {
      this.isAuthenticated = isAuthenticated;
    });

    this.authService.currentUser$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadUserPreferences();
      }
    });
  }

  private setupRouteTracking(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: NavigationEnd) => {
      // Track page views
      this.monitoringService.trackUserInteraction('page_view', 'router', {
        url: event.url,
        timestamp: new Date().toISOString()
      });
      
      // Close mobile menu on navigation
      this.showMobileMenu = false;
    });
  }

  private setupSyncStatusListener(): void {
    this.dataSyncService.syncStatus$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(status => {
      this.syncStatus = status;
      this.showSyncIndicator = status.pendingSync || !status.isOnline;
      
      // Show offline notification
      if (!status.isOnline && status.isOnline !== undefined) {
        this.notificationService.showNotification({
          title: 'You\'re Offline',
          message: 'Your progress will be saved and synced when you\'re back online.',
          type: 'info',
          duration: 0 // Don't auto-dismiss
        });
      }
    });
  }

  private setupLoadingIndicator(): void {
    this.loadingInterceptor.setLoadingCallback((loading: boolean) => {
      // Debounce loading indicator to prevent flicker
      setTimeout(() => {
        this.isLoading = loading;
      }, loading ? 200 : 0);
    });
  }

  private setupErrorHandling(): void {
    // Global error handler is already set up in MonitoringService
    window.addEventListener('error', (event) => {
      console.error('Global error:', event.error);
    });

    window.addEventListener('unhandledrejection', (event) => {
      console.error('Unhandled promise rejection:', event.reason);
    });
  }

  private initializeNotifications(): void {
    // Request notification permission for PWA
    if ('Notification' in window && Notification.permission === 'default') {
      this.notificationService.requestPermission().then(permission => {
        if (permission === 'granted') {
          console.log('Notification permission granted');
        }
      });
    }
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

  private initializePWA(): void {
    // Service worker update check
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('controllerchange', () => {
        this.notificationService.showNotification({
          title: 'App Updated',
          message: 'The app has been updated to the latest version.',
          type: 'info',
          action: {
            text: 'Reload',
            callback: () => window.location.reload()
          }
        });
      });
    }

    // Install prompt handling
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      
      this.notificationService.showNotification({
        title: 'Install App',
        message: 'Install our app for a better learning experience!',
        type: 'info',
        action: {
          text: 'Install',
          callback: () => {
            (e as any).prompt();
          }
        },
        duration: 10000
      });
    });
  }

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

  onSyncRetry(): void {
    this.dataSyncService.forceSyncAll().subscribe({
      next: () => {
        this.notificationService.showSuccessNotification('Sync completed successfully!');
      },
      error: () => {
        this.notificationService.showErrorNotification('Sync failed. Please try again later.');
      }
    });
  }

  logout(): void {
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

// Error boundary simulation (Angular doesn't have built-in error boundaries like React)
handleGlobalError(error: any): void {
  console.error('Global app error:', error);
  this.monitoringService.logError(error, 'app-component');
  
  this.notificationService.showErrorNotification(
    'An unexpected error occurred. The team has been notified.',
    'Application Error'
  );
}

// Performance monitoring
trackPerformanceMetric(name: string, value: number): void {
  this.monitoringService.logPerformanceMetric(name, value);
}

// Feature flag handling
isFeatureEnabled(feature: string): boolean {
  return this.configService.isFeatureEnabled(feature as any);
}
}