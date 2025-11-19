import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { RoleService } from '../services/role.service';
import { AuthService } from '../services/auth.service';

/**
 * Route guard for RO Dashboard pages.
 * Checks if user has RO role or any project role (PM, ADM, Offshore DD).
 */
@Injectable({
  providedIn: 'root'
})
export class RODashboardGuard implements CanActivate {
  
  constructor(
    private roleService: RoleService,
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    
    if (!this.authService.isLoggedIn()) {
      console.warn('RODashboardGuard: User not authenticated, redirecting to login');
      this.router.navigate(['/login']);
      return false;
    }

    if (this.roleService.canAccessRODashboard()) {
      return true;
    }

    return this.roleService.fetchUserRoles().pipe(
      take(1),
      map(roles => {
        const hasAccess = this.roleService.canAccessRODashboard();
        
        if (!hasAccess) {
          console.warn('RODashboardGuard: User does not have RO or project role, redirecting to home');
          this.router.navigate(['/home']);
        }
        
        return hasAccess;
      })
    );
  }
}
