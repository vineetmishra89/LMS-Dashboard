import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { RoleService } from '../services/role.service';
import { AuthService } from '../services/auth.service';

/**
 * Route guard for L&D Admin pages.
 * Checks if user has ROLE_LND_ADMIN role.
 */
@Injectable({
  providedIn: 'root'
})
export class LndAdminGuard implements CanActivate {
  
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
      console.warn('LndAdminGuard: User not authenticated, redirecting to login');
      this.router.navigate(['/login']);
      return false;
    }

    if (this.roleService.isLndAdmin()) {
      return true;
    }

    return this.roleService.fetchUserRoles().pipe(
      take(1),
      map(roles => {
        const hasAccess = this.roleService.isLndAdmin();
        
        if (!hasAccess) {
          console.warn('LndAdminGuard: User does not have L&D Admin role, redirecting to home');
          this.router.navigate(['/home']);
        }
        
        return hasAccess;
      })
    );
  }
}
