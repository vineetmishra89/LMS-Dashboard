import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { RegisterComponent } from './components/register/register.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { DashboardComponent } from './components/dashboard/dashboard';
import { UnauthorizedComponent } from './components/shared/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/shared/not-found/not-found.component';

const routes: Routes = [
  // Public Routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'verify-email/:token', component: VerifyEmailComponent },
  
  // Protected Routes
  { 
    path: '', 
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'courses', component: CoursesComponent },
      { path: 'course/:id', component: CourseDetailComponent },
      { path: 'certificates', component: CertificatesComponent },
      { path: 'analytics', component: AnalyticsComponent },
      
      // Admin Routes
      {
        path: 'admin',
        canActivate: [RoleGuard],
        data: { role: 'admin' },
        loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
      },
      
      // Instructor Routes
      {
        path: 'instructor',
        canActivate: [RoleGuard],
        data: { role: 'instructor' },
        loadChildren: () => import('./instructor/instructor.module').then(m => m.InstructorModule)
      }
    ]
  },
  
  // Error Routes
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: '/404' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    enableTracing: false,
    preloadingStrategy: PreloadAllModules,
    scrollPositionRestoration: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }