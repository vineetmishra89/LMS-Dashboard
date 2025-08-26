import { Injectable } from '@angular/core';
import { Observable, fromEvent } from 'rxjs';
import { throttleTime, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {
  private performanceObserver?: PerformanceObserver;
  
  constructor() {
    this.setupPerformanceObserver();
  }

  private setupPerformanceObserver(): void {
    if ('PerformanceObserver' in window) {
      this.performanceObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          this.logPerformanceEntry(entry);
        });
      });

      this.performanceObserver.observe({ 
        entryTypes: ['measure', 'navigation', 'resource', 'paint'] 
      });
    }
  }

  measureFunction<T>(name: string, fn: () => T): T {
    performance.mark(`${name}-start`);
    const result = fn();
    performance.mark(`${name}-end`);
    performance.measure(name, `${name}-start`, `${name}-end`);
    return result;
  }

  async measureAsyncFunction<T>(name: string, fn: () => Promise<T>): Promise<T> {
    performance.mark(`${name}-start`);
    const result = await fn();
    performance.mark(`${name}-end`);
    performance.measure(name, `${name}-start`, `${name}-end`);
    return result;
  }

  getPageLoadMetrics(): any {
    const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
    const paint = performance.getEntriesByType('paint');
    
    return {
      pageLoadTime: navigation.loadEventEnd - navigation.fetchStart,
      domContentLoaded: navigation.domContentLoadedEventEnd - navigation.fetchStart,
      firstPaint: paint.find(p => p.name === 'first-paint')?.startTime || 0,
      firstContentfulPaint: paint.find(p => p.name === 'first-contentful-paint')?.startTime || 0,
      timeToInteractive: navigation.domInteractive - navigation.fetchStart
    };
  }

  monitorScrollPerformance(): Observable<any> {
    return fromEvent(window, 'scroll').pipe(
      throttleTime(100),
      map(() => ({
        scrollY: window.scrollY,
        documentHeight: document.documentElement.scrollHeight,
        viewportHeight: window.innerHeight,
        timestamp: performance.now()
      }))
    );
  }

  private logPerformanceEntry(entry: PerformanceEntry): void {
    if (entry.entryType === 'measure') {
      console.log(`Performance: ${entry.name} took ${entry.duration.toFixed(2)}ms`);
    }
  }

  // Memory usage monitoring
  getMemoryUsage(): any {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      return {
        used: Math.round(memory.usedJSHeapSize / 1048576 * 100) / 100, // MB
        total: Math.round(memory.totalJSHeapSize / 1048576 * 100) / 100, // MB
        limit: Math.round(memory.jsHeapSizeLimit / 1048576 * 100) / 100 // MB
      };
    }
    return null;
  }
}