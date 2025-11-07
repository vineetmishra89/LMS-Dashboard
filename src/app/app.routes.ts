import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard';
import { NotFoundComponent } from './components/shared/not-found/not-found.component';
import { CompletedDetailComponent } from './components/details/completed-detail/completed-detail.component';
import { EnrolledDetailComponent } from './components/details/enrolled-detail/enrolled-detail.component';
import { HoursDetailComponent } from './components/details/hours-detail/hours-detail.component';
import { VideoPlayerPageComponent } from './pages/video-player/video-player-page.component';
//import { ViewCourseComponent } from './components/view-course/view-course.component';
import { PlayCourseComponent } from './components/play-course/play-course.component';
import { HomeComponent } from './components/home/home.component';
import { SearchComponent } from './components/search/search.component';
import { SupportComponent } from './components/support/support.component';
import { ViewCourseComponent } from './components/view-course/view-course.component';
import { LndAdminComponent } from './components/lnd-admin/lnd-admin';
import { LoginComponent } from './components/login/login.component';
import { RoAdminComponent } from './components/ro-admin/ro-admin.component';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'home', component: HomeComponent },
  { path: 'roAdmin', component: RoAdminComponent },
  { path: 'lndAdmin', component: LndAdminComponent },
  { path: 'search', component: DashboardComponent },
  { path: 'support', component: SupportComponent },
  { path: 'dashboard', component: SearchComponent },
  { path: 'viewCourse/:trainingId', component: ViewCourseComponent },
  { path: 'runningCourse/:trainingId', component: VideoPlayerPageComponent },
  { path: 'video-player/:courseId', component: VideoPlayerPageComponent },
  { path: 'detail/completed', component: CompletedDetailComponent },
  { path: 'detail/enrolled', component: EnrolledDetailComponent },
  { path: 'detail/hours', component: HoursDetailComponent },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollPositionRestoration: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
