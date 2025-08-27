import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { RoleGuard } from './guards/role.guard';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { DashboardComponent } from './components/dashboard/dashboard';
import { UnauthorizedComponent } from './components/shared/unauthorized/unauthorized.component';
import { NotFoundComponent } from './components/shared/not-found/not-found.component';
import { ProfileComponent } from './components/profile/profile.component';
import { CoursesComponent } from './components/courses/courses.component';
import { CertificatesComponent } from './components/certificates/certificates.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'courses', component: CoursesComponent },
  { path: 'certificates', component: CertificatesComponent },
  { path: 'analytics', component: AnalyticsComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
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
