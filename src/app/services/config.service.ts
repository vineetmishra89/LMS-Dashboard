import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface FeatureConfig {
  notifications: boolean;
  analytics: boolean;
  socialLogin: boolean;
  videoStreaming: boolean;
  offlineMode: boolean;
  chatSupport: boolean;
  darkMode: boolean;
  multiLanguage: boolean;
  paymentGateway: boolean;
  videoConferencing: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private configSubject = new BehaviorSubject<FeatureConfig>(environment.features);
  public config$ = this.configSubject.asObservable();

  constructor() {
    this.loadRemoteConfig();
  }

  private loadRemoteConfig(): void {
    // Load feature flags from backend or feature flag service
    // This allows enabling/disabling features without deployment
    fetch(`${environment.apiUrl}/config/features`)
      .then(response => response.json())
      .then(config => {
        this.configSubject.next({ ...environment.features, ...config });
      })
      .catch(error => {
        console.warn('Failed to load remote config, using defaults:', error);
      });
  }

  isFeatureEnabled(feature: keyof FeatureConfig): boolean {
    return this.configSubject.value[feature] ?? false;
  }

  getConfig(): FeatureConfig {
    return this.configSubject.value;
  }

  updateFeature(feature: keyof FeatureConfig, enabled: boolean): void {
    const currentConfig = this.configSubject.value;
    this.configSubject.next({
      ...currentConfig,
      [feature]: enabled
    });
  }
}