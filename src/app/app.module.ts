import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ServiceWorkerModule } from '@angular/service-worker';
import { CommonModule } from '@angular/common';

// Angular Material (Optional - for advanced UI components)
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

// NgRx Store
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';

// Routing
import { AppRoutingModule } from './app.routes';

import { CompletedDetailComponent } from './components/details/completed-detail/completed-detail.component';
import { EnrolledDetailComponent } from './components/details/enrolled-detail/enrolled-detail.component';
import { HoursDetailComponent } from './components/details/hours-detail/hours-detail.component';

// Core Components
import { AppComponent } from './app';
import { DashboardComponent } from './components/dashboard/dashboard';
import { HeaderComponent } from './components/header/header';
import { SidebarComponent } from './components/sidebar/sidebar';
import { WelcomeBannerComponent } from './components/welcome-banner/welcome-banner';
import { StatsCardComponent } from './components/stats-card/stats-card';
import { CourseCardComponent } from './components/course-card/course-card';
import { ChatComponent } from './components/chat/chat';

// Authentication Components
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';

// Feature Components
import { ProfileComponent } from './components/profile/profile.component';
import { CoursesComponent } from './components/courses/courses.component';
import { CertificatesComponent } from './components/certificates/certificates.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { NotificationsComponent } from './components/notifications/notifications.component';

// Shared Components
import { LoadingSpinnerComponent } from './components/shared/loading-spinner/loading-spinner.component';
import { ErrorDisplayComponent } from './components/shared/error-display/error-display.component';
import { ToastNotificationComponent } from './components/shared/toast-notification/toast-notification.component';
import { ConfirmDialogComponent } from './components/shared/confirm-dialog/confirm-dialog.component';
import { UnauthorizedComponent } from './components/shared/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/shared/not-found/not-found.component';

// Services
import { ApiService } from './services/api.service';
import { AuthService } from './services/auth.service';
import { UserService } from './services/user.service';
import { CourseService } from './services/course.service';
import { EnrollmentService } from './services/enrollment.service';
import { AnalyticsService } from './services/analytics.service';
import { CertificateService } from './services/certificate.service';
import { NotificationService } from './services/notification.service';
import { DataSyncService } from './services/data-sync.service';
import { WebSocketService } from './services/websocket.service';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { UnsavedChangesGuard } from './guards/unsaved-changes.guard';

// Interceptors
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { ErrorInterceptor } from './interceptors/error.interceptor';
import { LoadingInterceptor } from './interceptors/loading.interceptor';
import { CacheInterceptor } from './interceptors/cache.interceptor';

// Pipes
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { DurationPipe } from './pipes/duration.pipe';
import { ProgressPipe } from './pipes/progress.pipe';
import { TimeAgoPipe } from './pipes/time-ago.pipe';
import { SearchHighlightPipe } from './pipes/search-highlight.pipe';

// Directives
import { LazyLoadDirective } from './directives/lazy-load.directive';
import { InViewportDirective } from './directives/in-viewport.directive';
import { ClickOutsideDirective } from './directives/click-outside.directive';

// Environment
import { environment } from '../environments/environment';

@NgModule({
  declarations: [
    // Core Components
    AppComponent,
    DashboardComponent,
    HeaderComponent,
    SidebarComponent,
    WelcomeBannerComponent,
    StatsCardComponent,
    CourseCardComponent,
    ChatComponent,
    
    // Auth Components
    LoginComponent,
    RegisterComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    
    // Shared Components
    LoadingSpinnerComponent,
    ErrorDisplayComponent,
    ToastNotificationComponent,
    ConfirmDialogComponent,
    UnauthorizedComponent,
    NotFoundComponent,
    
    // Directives
    LazyLoadDirective,
    InViewportDirective,
    ClickOutsideDirective,

    TimeAgoPipe,
    SearchHighlightPipe,
    
    // Feature Components
    ProfileComponent,
    CoursesComponent,
    CertificatesComponent,
    AnalyticsComponent,
    NotificationsComponent,

    CompletedDetailComponent,
    EnrolledDetailComponent,
    HoursDetailComponent
  ],
  imports: [
    // Angular Core
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    
    // Routing
    AppRoutingModule,
    
    // Angular Material
    MatDialogModule,
    MatSnackBarModule,
    MatProgressBarModule,
    MatChipsModule,
    MatAutocompleteModule,

    SafeHtmlPipe,
    DurationPipe,
    ProgressPipe,

    // PWA Support
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      registrationStrategy: 'registerWhenStable:30000'
    }),
    
    // State Management
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    
    // Development Tools
    !environment.production ? StoreDevtoolsModule.instrument({
      maxAge: 25,
      logOnly: false,
      connectInZone: true
    }) : []
  ],
  providers: [
    // Core Services
    ApiService,
    AuthService,
    UserService,
    CourseService,
    EnrollmentService,
    AnalyticsService,
    CertificateService,
    NotificationService,
    DataSyncService,
    WebSocketService,
    
    LoadingInterceptor,
    
    // Guards
    AuthGuard,
    RoleGuard,
    UnsavedChangesGuard,
    
    // HTTP Interceptors
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoadingInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CacheInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
