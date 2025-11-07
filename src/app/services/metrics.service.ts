import { inject, Injectable } from '@angular/core';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { CourseDetail, CourseCategory, CourseFilters, CourseMaster } from '../models/course';
import { ApiService } from './api.service';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {
  apiService = inject(ApiService);
  httpClient = inject(HttpClient);

  http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  getMetrics(data: any): Observable<any> {
    
    return this.apiService.post<any>(`metrics/report`, data);
  }
  
  getMetricsDownload(data: any): Observable<any> {
    const endpoint = `metrics/report/download`;
    const url = this.buildParamQueryModern(`${this.baseUrl}/${endpoint}`, data);
    // return this.apiService.get(req, {
    //   responseType: 'blob'
    // });
//     const params = this.buildParamQueryModern(`metrics/report/download`, data);
// return this.httpClient.get('metrics/report/download', {
//   responseType: 'blob'
// });
return this.httpClient.get<Blob>(url, {
  observe: 'response',
  responseType: 'blob' as 'json'
})

  }
  
  buildParamQueryModern(baseUrl: any, params: any) {
    const searchParams = new URLSearchParams();
  
    for (const key in params) {
      const value = params[key];
      // Check for valid values before appending
      if (value !== null && value !== undefined && value !== '') {
        // URLSearchParams handles the encoding (like encodeURIComponent) automatically
        searchParams.append(key, value);
      }
    }
  
    const queryString = searchParams.toString();
    
    if (queryString) {
      return `${baseUrl}?${queryString}`;
    }
    
    return baseUrl;
  }

  
}
