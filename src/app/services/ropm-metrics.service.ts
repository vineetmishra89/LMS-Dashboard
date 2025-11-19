import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RopmMetricsService {

  constructor(
    private apiService: ApiService,
    private http: HttpClient
  ) { }

  getMetrics(data: any): Observable<any> {
    return this.apiService.post<any>(`ro-pm-dashboard/metrics/report`, data);
  }

  getMetricsDownload(data: any): Observable<any> {
    const params: any = {};
    
    if (data.category) params.category = data.category;
    if (data.level) params.level = data.level;
    if (data.technology) params.technology = data.technology;
    if (data.startDate) params.startDate = data.startDate;
    if (data.endDate) params.endDate = data.endDate;
    if (data.topN) params.topN = data.topN;

    const queryString = new URLSearchParams(params).toString();
    const url = `${environment.apiUrl}/ro-pm-dashboard/metrics/report/download${queryString ? '?' + queryString : ''}`;

    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get(url, {
      headers: headers,
      observe: 'response',
      responseType: 'blob'
    });
  }
}
