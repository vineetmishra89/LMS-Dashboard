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

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'lndAdmin', component: LndAdminComponent },
  { path: 'search', component: DashboardComponent },
  { path: 'support', component: SupportComponent },
  { path: 'dashboard', component: SearchComponent },
  { path: 'viewCourse', component: ViewCourseComponent },
  { path: 'runningCourse', component: PlayCourseComponent },
  { path: 'video-player/:courseId', component: VideoPlayerPageComponent },
  { path: 'detail/completed', component: CompletedDetailComponent },
  { path: 'detail/enrolled', component: EnrolledDetailComponent },
  { path: 'detail/hours', component: HoursDetailComponent },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollPositionRestoration: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
