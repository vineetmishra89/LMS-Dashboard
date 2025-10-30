import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, map, retry, timeout } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errors?: string[];
  pagination?: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = environment.apiUrl;
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  private handleError(operation = 'operation') {
    return (error: any): Observable<never> => {
      console.error(`${operation} failed:`, error);
      this.loadingSubject.next(false);
      return throwError(() => error);
    };
  }

  private setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  get<T>(endpoint: string, params?: HttpParams): Observable<T> {
    this.setLoading(true);
    return this.http.get<T>(`${this.baseUrl}/${endpoint}`, {
      headers: this.getHeaders(),
      params
    }).pipe(
     // timeout(30000),
      //retry(2),
      map(response => {
        this.setLoading(false);
        return response;
      }),
      catchError(this.handleError(`GET ${endpoint}`))
    );
  }

  post<T>(endpoint: string, data: any): Observable<T> {
    this.setLoading(true);
    return this.http.post<T>(`${this.baseUrl}/${endpoint}`, data, {
      headers: this.getHeaders()
    }).pipe(
    //  timeout(30000),
      map(response => {
        this.setLoading(false);
        return response;
      }),
      catchError(this.handleError(`POST ${endpoint}`))
    );
  }

  put<T>(endpoint: string, data: any): Observable<T> {
    this.setLoading(true);
    return this.http.put<T>(`${this.baseUrl}/${endpoint}`, data, {
      headers: this.getHeaders()
    }).pipe(
      timeout(30000),
      map(response => {
        this.setLoading(false);
        return response;
      }),
      catchError(this.handleError(`PUT ${endpoint}`))
    );
  }

  delete<T>(endpoint: string): Observable<T> {
    this.setLoading(true);
    return this.http.delete<T>(`${this.baseUrl}/${endpoint}`, {
      headers: this.getHeaders()
    }).pipe(
     // timeout(30000),
      map(response => {
        this.setLoading(false);
        return response;
      }),
      catchError(this.handleError(`DELETE ${endpoint}`))
    );
  }
}
