import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface ErrorLog {
  message: string;
  stack?: string;
  url: string;
  timestamp: Date;
  userId?: string;
  userAgent: string;
  sessionId: string;
  context: any;
}

@Injectable({
  providedIn: 'root'
})
export class MonitoringService {
  private sessionId: string;
  private errorQueue: ErrorLog[] = [];
  private readonly maxQueueSize = 50;

  constructor(private router: Router) {
    this.sessionId = this.generateSessionId();
    this.setupGlobalErrorHandler();
    this.setupPerformanceMonitoring();
  }

  logError(error: Error, context?: any): void {
    const errorLog: ErrorLog = {
      message: error.message,
      stack: error.stack,
      url: window.location.href,
      timestamp: new Date(),
      userId: localStorage.getItem('userId') || undefined,
      userAgent: navigator.userAgent,
      sessionId: this.sessionId,
      context: context || {}
    };

    // Add to queue
    this.errorQueue.push(errorLog);
    if (this.errorQueue.length > this.maxQueueSize) {
      this.errorQueue.shift(); // Remove oldest
    }

    // Send to monitoring service
    this.sendErrorToService(errorLog);
  }

  logUserAction(action: string, details?: any): void {
    const actionLog = {
      action,
      details,
      timestamp: new Date(),
      url: window.location.href,
      userId: localStorage.getItem('userId'),
      sessionId: this.sessionId
    };

    // Send to analytics
    this.sendActionToService(actionLog);
  }

  logPerformanceMetric(name: string, value: number, unit: string = 'ms'): void {
    const metric = {
      name,
      value,
      unit,
      timestamp: new Date(),
      url: window.location.href,
      sessionId: this.sessionId
    };

    this.sendMetricToService(metric);
  }

  private setupGlobalErrorHandler(): void {
    window.addEventListener('error', (event) => {
      this.logError(event.error, {
        type: 'global',
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno
      });
    });

    window.addEventListener('unhandledrejection', (event) => {
      this.logError(new Error(event.reason), {
        type: 'unhandled_promise_rejection'
      });
    });
  }

  private setupPerformanceMonitoring(): void {
    // Monitor page load times
    window.addEventListener('load', () => {
      setTimeout(() => {
        const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
        this.logPerformanceMetric('page_load_time', perfData.loadEventEnd - perfData.fetchStart);
        this.logPerformanceMetric('dom_content_loaded', perfData.domContentLoadedEventEnd - perfData.fetchStart);
      }, 0);
    });

    // Monitor route changes
    this.router.events.subscribe(event => {
      if (event.constructor.name === 'NavigationEnd') {
        this.logUserAction('route_change', { url: event.toString() });
      }
    });
  }

  private sendErrorToService(errorLog: ErrorLog): void {
    if (environment.production) {
      // Send to error tracking service (Sentry, LogRocket, etc.)
      fetch(`${environment.apiUrl}/monitoring/errors`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(errorLog)
      }).catch(err => {
        console.error('Failed to send error to monitoring service:', err);
      });
    } else {
      console.error('Error logged:', errorLog);
    }
  }

  private sendActionToService(actionLog: any): void {
    if (environment.production) {
      fetch(`${environment.apiUrl}/analytics/actions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(actionLog)
      }).catch(err => {
        console.warn('Failed to send action to analytics:', err);
      });
    }
  }

  private sendMetricToService(metric: any): void {
    if (environment.production) {
      fetch(`${environment.apiUrl}/monitoring/metrics`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(metric)
      }).catch(err => {
        console.warn('Failed to send metric to monitoring:', err);
      });
    }
  }

  private generateSessionId(): string {
    return `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  // Public methods for component usage
  trackComponentLoad(componentName: string): void {
    this.logUserAction('component_load', { component: componentName });
  }

  trackUserInteraction(interaction: string, element: string, details?: any): void {
    this.logUserAction('user_interaction', {
      interaction,
      element,
      ...details
    });
  }

  getErrorQueue(): ErrorLog[] {
    return [...this.errorQueue];
  }

  clearErrorQueue(): void {
    this.errorQueue = [];
  }
}