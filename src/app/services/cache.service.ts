import { Injectable } from '@angular/core';
import { Observable, of, from } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { StorageService } from './storage.service';

interface CacheItem<T> {
  data: T;
  timestamp: Date;
  expiresAt: Date;
  version: string;
}

@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private readonly cacheVersion = '1.0.0';
  private readonly defaultTTL = 5 * 60 * 1000; // 5 minutes

  constructor(private storageService: StorageService) {}

  set<T>(key: string, data: T, ttl?: number): Observable<void> {
    const expirationTime = ttl || this.defaultTTL;
    const cacheItem: CacheItem<T> = {
      data,
      timestamp: new Date(),
      expiresAt: new Date(Date.now() + expirationTime),
      version: this.cacheVersion
    };

    return from(this.storageService.setIndexedDBItem('cache', key, cacheItem)).pipe(
      catchError(error => {
        console.warn('Failed to cache data:', error);
        return of(void 0);
      })
    );
  }

  get<T>(key: string): Observable<T | null> {
    return from(this.storageService.getIndexedDBItem<CacheItem<T>>('cache', key)).pipe(
      map(cacheItem => {
        if (!cacheItem) return null;
        
        // Check version compatibility
        if (cacheItem.version !== this.cacheVersion) {
          this.delete(key);
          return null;
        }
        
        // Check expiration
        if (new Date() > cacheItem.expiresAt) {
          this.delete(key);
          return null;
        }
        
        return cacheItem.data;
      }),
      catchError(error => {
        console.warn('Failed to retrieve cached data:', error);
        return of(null);
      })
    );
  }

  delete(key: string): Observable<void> {
    return from(this.storageService.setIndexedDBItem('cache', key, null));
  }

  clear(): Observable<void> {
    return from(indexedDB.deleteDatabase('LMSDatabase'));
  }

  // Cache with fallback to API
  getOrFetch<T>(
    key: string,
    fetchFunction: () => Observable<T>,
    ttl?: number
  ): Observable<T> {
    return this.get<T>(key).pipe(
      switchMap(cachedData => {
        if (cachedData !== null) {
          return of(cachedData);
        }
        
        // Fetch from API and cache
        return fetchFunction().pipe(
          tap(data => {
            this.set(key, data, ttl).subscribe();
          })
        );
      })
    );
  }

  // Preload frequently accessed data
  preloadEssentialData(userId: string): Observable<any> {
    // Preload user data, enrollments, etc.
    const preloadKeys = [
      `user-${userId}`,
      `enrollments-${userId}`,
      `analytics-${userId}`,
      `trending-courses`,
      `categories`
    ];

    return from(Promise.all(
      preloadKeys.map(key => this.get(key).toPromise())
    ));
  }
}