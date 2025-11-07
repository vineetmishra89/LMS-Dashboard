import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { Observable } from 'rxjs';

export interface ComponentCanDeactivate {
  canDeactivate: () => Observable<boolean> | Promise<boolean> | boolean;
  hasUnsavedChanges?: () => boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UnsavedChangesGuard implements CanDeactivate<ComponentCanDeactivate> {
  
  canDeactivate(
    component: ComponentCanDeactivate
  ): Observable<boolean> | Promise<boolean> | boolean {
    
    // Check if component has unsaved changes
    if (component.hasUnsavedChanges && component.hasUnsavedChanges()) {
      return confirm('You have unsaved changes. Are you sure you want to leave this page?');
    }
    
    // Use component's own canDeactivate method if available
    if (component.canDeactivate) {
      return component.canDeactivate();
    }
    
    return true;
  }
}