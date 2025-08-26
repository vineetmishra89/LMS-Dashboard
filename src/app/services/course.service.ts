import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Course, CourseCategory, CourseFilters } from '../models/course';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private coursesSubject = new BehaviorSubject<Course[]>([]);
  private categoriesSubject = new BehaviorSubject<CourseCategory[]>([]);
  private filtersSubject = new BehaviorSubject<CourseFilters>({
    category: [],
    level: [],
    priceRange: { min: 0, max: 1000 },
    rating: 0,
    duration: { min: 0, max: 1000 },
    instructor: []
  });

  public courses$ = this.coursesSubject.asObservable();
  public categories$ = this.categoriesSubject.asObservable();
  public filters$ = this.filtersSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.loadCategories();
  }

  // Course Retrieval
  getAllCourses(params?: any): Observable<Course[]> {
    return this.apiService.get<Course[]>('courses', params).pipe(
      tap(courses => this.coursesSubject.next(courses))
    );
  }

  getCourseById(courseId: string): Observable<Course> {
    return this.apiService.get<Course>(`courses/${courseId}`);
  }

  getEnrolledCourses(userId: string): Observable<Course[]> {
    return this.apiService.get<Course[]>(`users/${userId}/enrolled-courses`);
  }

  getTrendingCourses(limit: number = 10): Observable<Course[]> {
    return this.apiService.get<Course[]>(`courses/trending?limit=${limit}`);
  }

  getCoursesByCategory(categoryId: string): Observable<Course[]> {
    return this.apiService.get<Course[]>(`courses/category/${categoryId}`);
  }

  getCoursesByInstructor(instructorId: string): Observable<Course[]> {
    return this.apiService.get<Course[]>(`courses/instructor/${instructorId}`);
  }

  // Search and Filtering
  searchCourses(query: string, filters?: CourseFilters): Observable<Course[]> {
    const params = this.buildSearchParams(query, filters);
    return this.apiService.get<Course[]>('courses/search', params);
  }

  private buildSearchParams(query: string, filters?: CourseFilters): any {
    const params: any = { q: query };
    
    if (filters) {
      if (filters.category.length) params.categories = filters.category.join(',');
      if (filters.level.length) params.levels = filters.level.join(',');
      if (filters.rating) params.minRating = filters.rating;
      if (filters.priceRange) {
        params.minPrice = filters.priceRange.min;
        params.maxPrice = filters.priceRange.max;
      }
      if (filters.duration) {
        params.minDuration = filters.duration.min;
        params.maxDuration = filters.duration.max;
      }
    }
    
    return params;
  }

  updateFilters(filters: Partial<CourseFilters>): void {
    const currentFilters = this.filtersSubject.value;
    this.filtersSubject.next({ ...currentFilters, ...filters });
  }

  clearFilters(): void {
    this.filtersSubject.next({
      category: [],
      level: [],
      priceRange: { min: 0, max: 1000 },
      rating: 0,
      duration: { min: 0, max: 1000 },
      instructor: []
    });
  }

  // Categories
  loadCategories(): Observable<CourseCategory[]> {
    return this.apiService.get<CourseCategory[]>('categories').pipe(
      tap(categories => this.categoriesSubject.next(categories))
    );
  }

  // Course Reviews
  getCourseReviews(courseId: string, page: number = 1): Observable<any> {
    return this.apiService.get(`courses/${courseId}/reviews?page=${page}`);
  }

  submitCourseReview(courseId: string, review: any): Observable<any> {
    return this.apiService.post(`courses/${courseId}/reviews`, review);
  }
}