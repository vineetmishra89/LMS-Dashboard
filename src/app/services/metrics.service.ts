import { inject, Injectable } from '@angular/core';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { CourseDetail, CourseCategory, CourseFilters, CourseMaster } from '../models/course';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {
  apiService = inject(ApiService);

  getMetrics(data: any): Observable<any> {
    
    return this.apiService.get<any>(`metrics/report?timePeriod=LAST_6_MONTHS&topN=10`);
  }

  
}
