import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

interface CacheEntry {
  response: HttpResponse<any>;
  timestamp: Date;
  ttl: number;
}

@Injectable()
export class CacheInterceptor implements HttpInterceptor {
  private cache = new Map<string, CacheEntry>();
  private readonly defaultTTL = 5 * 60 * 1000; // 5 minutes
  
  // Define which endpoints should be cached
  private cacheableEndpoints = [
    '/api/courses',
    '/api/categories',
    '/api/instructors'
  ];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Only cache GET requests
    if (req.method !== 'GET') {
      return next.handle(req);
    }

    // Check if this endpoint should be cached
    const shouldCache = this.cacheableEndpoints.some(endpoint => 
      req.url.includes(endpoint)
    );

    if (!shouldCache) {
      return next.handle(req);
    }

    // Check for cached response
    const cacheKey = this.generateCacheKey(req);
    const cachedEntry = this.cache.get(cacheKey);

    if (cachedEntry && this.isCacheValid(cachedEntry)) {
      console.log('Serving from cache:', req.url);
      return of(cachedEntry.response.clone());
    }

    // Make request and cache response
    return next.handle(req).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          const ttl = this.getTTLForEndpoint(req.url);
          this.cache.set(cacheKey, {
            response: event.clone(),
            timestamp: new Date(),
            ttl
          });
          
          console.log('Cached response for:', req.url);
        }
      })
    );
  }

  private generateCacheKey(req: HttpRequest<any>): string {
    return `${req.method}:${req.url}:${JSON.stringify(req.params)}`;
  }

  private isCacheValid(entry: CacheEntry): boolean {
    const now = Date.now();
    const entryTime = entry.timestamp.getTime();
    return (now - entryTime) < entry.ttl;
  }

  private getTTLForEndpoint(url: string): number {
    if (url.includes('/courses')) return 10 * 60 * 1000; // 10 minutes
    if (url.includes('/categories')) return 30 * 60 * 1000; // 30 minutes
    if (url.includes('/instructors')) return 15 * 60 * 1000; // 15 minutes
    
    return this.defaultTTL;
  }

  clearCache(): void {
    this.cache.clear();
    console.log('Cache cleared');
  }

  clearCacheForUrl(url: string): void {
    const keysToDelete = Array.from(this.cache.keys()).filter(key => key.includes(url));
    keysToDelete.forEach(key => this.cache.delete(key));
    console.log(`Cleared cache for: ${url}`);
  }

  getCacheSize(): number {
    return this.cache.size;
  }
}