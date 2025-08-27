import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard';
import { NotFoundComponent } from './components/shared/not-found/not-found.component';
import { CompletedDetailComponent } from './components/details/completed-detail/completed-detail.component';
import { EnrolledDetailComponent } from './components/details/enrolled-detail/enrolled-detail.component';
import { HoursDetailComponent } from './components/details/hours-detail/hours-detail.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'detail/completed', component: CompletedDetailComponent },
  { path: 'detail/enrolled', component: EnrolledDetailComponent },
  { path: 'detail/hours', component: HoursDetailComponent },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: '/404' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollPositionRestoration: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
