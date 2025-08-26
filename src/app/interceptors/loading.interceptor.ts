import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, finalize } from 'rxjs/operators';

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private activeRequests = 0;
  private loadingCallback: ((loading: boolean) => void) | null = null;

  setLoadingCallback(callback: (loading: boolean) => void): void {
    this.loadingCallback = callback;
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Don't show loading for certain endpoints
    const skipLoading = req.url.includes('/notifications') || 
                       req.url.includes('/analytics/track') ||
                       req.headers.has('X-Skip-Loading');

    if (!skipLoading) {
      this.activeRequests++;
      this.updateLoadingStatus(true);
    }

    return next.handle(req).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          // Request completed successfully
        }
      }),
      finalize(() => {
        if (!skipLoading) {
          this.activeRequests--;
          if (this.activeRequests === 0) {
            this.updateLoadingStatus(false);
          }
        }
      })
    );
  }

  private updateLoadingStatus(loading: boolean): void {
    if (this.loadingCallback) {
      this.loadingCallback(loading);
    }
  }

  getActiveRequestCount(): number {
    return this.activeRequests;
  }
}