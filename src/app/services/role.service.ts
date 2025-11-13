import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { UserRoles } from '../models/user-roles.model';
import { RoleConstants } from '../constants/role.constants';
import { environment } from '../../environments/environment';

/**
 * Service for managing user roles.
 * Fetches roles from backend and provides methods to check role membership.
 */
@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly ROLES_CACHE_KEY = 'userRoles';
  private rolesSubject = new BehaviorSubject<string[]>([]);
  
  public roles$ = this.rolesSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadRolesFromCache();
  }

  /**
   * Fetch user roles from backend API.
   * Caches the result in localStorage and BehaviorSubject.
   * 
   * @returns Observable of role strings array
   */
  fetchUserRoles(): Observable<string[]> {
    return this.http.get<UserRoles>(`${environment.apiUrl}/user/roles`).pipe(
      map(response => response.roles || []),
      tap(roles => {
        this.rolesSubject.next(roles);
        this.cacheRoles(roles);
        console.log('User roles fetched:', roles);
      }),
      catchError(error => {
        console.error('Error fetching user roles:', error);
        return of([]);
      })
    );
  }

  /**
   * Get current roles synchronously from the BehaviorSubject.
   * 
   * @returns Array of role strings
   */
  getCurrentRoles(): string[] {
    return this.rolesSubject.value;
  }

  /**
   * Check if user has a specific role.
   * 
   * @param role Role constant to check (e.g., RoleConstants.ROLE_LND_ADMIN)
   * @returns true if user has the role, false otherwise
   */
  hasRole(role: string): boolean {
    const roles = this.getCurrentRoles();
    return roles.includes(role);
  }

  /**
   * Check if user has any of the specified roles.
   * 
   * @param roles Array of role constants to check
   * @returns true if user has at least one of the roles, false otherwise
   */
  hasAnyRole(...roles: string[]): boolean {
    const userRoles = this.getCurrentRoles();
    return roles.some(role => userRoles.includes(role));
  }

  /**
   * Check if user has all of the specified roles.
   * 
   * @param roles Array of role constants to check
   * @returns true if user has all of the roles, false otherwise
   */
  hasAllRoles(...roles: string[]): boolean {
    const userRoles = this.getCurrentRoles();
    return roles.every(role => userRoles.includes(role));
  }

  /**
   * Check if user is an L&D Admin.
   * 
   * @returns true if user has L&D Admin role
   */
  isLndAdmin(): boolean {
    return this.hasRole(RoleConstants.ROLE_LND_ADMIN);
  }

  /**
   * Check if user is a Reporting Officer.
   * 
   * @returns true if user has RO role
   */
  isRO(): boolean {
    return this.hasRole(RoleConstants.ROLE_RO);
  }

  /**
   * Check if user has any project role (PM, ADM, Offshore DD).
   * 
   * @returns true if user has any project role
   */
  hasProjectRole(): boolean {
    return this.hasAnyRole(
      RoleConstants.ROLE_PROJECT_MANAGER,
      RoleConstants.ROLE_PROJECT_ADMIN,
      RoleConstants.ROLE_OFFSHORE_DD
    );
  }

  /**
   * Check if user should see RO Dashboard.
   * RO Dashboard is visible to RO or any project role holder.
   * 
   * @returns true if user should see RO Dashboard
   */
  canAccessRODashboard(): boolean {
    return this.hasAnyRole(
      RoleConstants.ROLE_RO,
      RoleConstants.ROLE_PROJECT_MANAGER,
      RoleConstants.ROLE_PROJECT_ADMIN,
      RoleConstants.ROLE_OFFSHORE_DD
    );
  }

  /**
   * Clear roles from cache and memory.
   * Call this on logout.
   */
  clearRoles(): void {
    this.rolesSubject.next([]);
    localStorage.removeItem(this.ROLES_CACHE_KEY);
  }

  /**
   * Load roles from localStorage cache.
   * Called on service initialization.
   */
  private loadRolesFromCache(): void {
    try {
      const cachedRoles = localStorage.getItem(this.ROLES_CACHE_KEY);
      if (cachedRoles) {
        const roles = JSON.parse(cachedRoles);
        this.rolesSubject.next(roles);
        console.log('Loaded roles from cache:', roles);
      }
    } catch (error) {
      console.error('Error loading roles from cache:', error);
    }
  }

  /**
   * Cache roles in localStorage.
   * 
   * @param roles Array of role strings to cache
   */
  private cacheRoles(roles: string[]): void {
    try {
      localStorage.setItem(this.ROLES_CACHE_KEY, JSON.stringify(roles));
    } catch (error) {
      console.error('Error caching roles:', error);
    }
  }
}
