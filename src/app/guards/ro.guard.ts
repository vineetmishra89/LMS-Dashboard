import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RoGuard implements CanActivate {
  
  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    const userId = localStorage.getItem('userId');
    
    if (!userId) {
      console.warn('No userId found in localStorage, redirecting to login');
      this.router.navigate(['/login']);
      return of(false);
    }

    return this.http.get<{ isRo: boolean }>(`${environment.apiUrl}/employee-hierarchy/is-ro?userId=${userId}`)
      .pipe(
        map(response => {
          if (response.isRo) {
            return true;
          } else {
            console.warn('User is not an RO, redirecting to home');
            this.router.navigate(['/home']);
            return false;
          }
        }),
        catchError(error => {
          console.error('Error checking RO status:', error);
          this.router.navigate(['/home']);
          return of(false);
        })
      );
  }
}
