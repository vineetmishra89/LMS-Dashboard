import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, BehaviorSubject, combineLatest } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Course, CourseCategory, CourseFilters } from '../../models/course';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-courses',
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.scss']
})
export class CoursesComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new BehaviorSubject<string>('');
  
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  categories: CourseCategory[] = [];
  filters: CourseFilters = {
    category: [],
    level: [],
    priceRange: { min: 0, max: 1000 },
    rating: 0,
    duration: { min: 0, max: 1000 },
    instructor: []
  };
  
  searchQuery = '';
  isLoading = false;
  currentPage = 1;
  pageSize = 12;
  totalCourses = 0;
  viewMode: 'grid' | 'list' = 'grid';
  sortBy: 'popular' | 'rating' | 'newest' | 'price_low' | 'price_high' = 'popular';
  
  // Filter panel
  showFilters = false;
  priceRange = { min: 0, max: 1000 };

  constructor(
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.initializeSearch();
    this.loadCategories();
    this.loadCourses();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeSearch(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(query => {
      this.performSearch(query);
    });
  }

  private loadCategories(): void {
    this.courseService.loadCategories().subscribe(categories => {
      this.categories = categories;
    });
  }

  private loadCourses(): void {
    this.isLoading = true;
    
    const params = {
      page: this.currentPage,
      limit: this.pageSize,
      sortBy: this.sortBy,
      ...this.buildFilterParams()
    };

    this.courseService.getAllCourses(params).subscribe({
      next: (response: any) => {
        this.courses = response.courses || response;
        this.totalCourses = response.total || this.courses.length;
        this.filteredCourses = this.courses;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load courses:', error);
        this.isLoading = false;
      }
    });
  }

  onSearch(query: string): void {
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  private performSearch(query: string): void {
    if (!query.trim()) {
      this.filteredCourses = this.courses;
      return;
    }

    this.isLoading = true;
    this.courseService.searchCourses(query, this.filters).subscribe({
      next: (courses) => {
        this.filteredCourses = courses;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Search failed:', error);
        this.isLoading = false;
      }
    });
  }

  onFilterChange(filterType: keyof CourseFilters, value: any): void {
    this.filters = { ...this.filters, [filterType]: value };
    this.courseService.updateFilters({ [filterType]: value });
    this.currentPage = 1;
    this.loadCourses();
  }

  onSortChange(sortBy: string): void {
    this.sortBy = sortBy as any;
    this.currentPage = 1;
    this.loadCourses();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadCourses();
  }

  onEnrollInCourse(courseId: string): void {
    const currentUser = this.userService.getCurrentUser();
    if (currentUser) {
      this.enrollmentService.enrollInCourse({
        userId: currentUser.id,
        courseId: courseId
      }).subscribe({
        next: (enrollment) => {
          console.log('Successfully enrolled:', enrollment);
          // Update UI to reflect enrollment
        },
        error: (error) => {
          console.error('Enrollment failed:', error);
        }
      });
    }
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  clearFilters(): void {
    this.filters = {
      category: [],
      level: [],
      priceRange: { min: 0, max: 1000 },
      rating: 0,
      duration: { min: 0, max: 1000 },
      instructor: []
    };
    this.courseService.clearFilters();
    this.loadCourses();
  }

  private buildFilterParams(): any {
    const params: any = {};
    
    if (this.filters.category.length) params.categories = this.filters.category.join(',');
    if (this.filters.level.length) params.levels = this.filters.level.join(',');
    if (this.filters.rating > 0) params.minRating = this.filters.rating;
    if (this.filters.priceRange.min > 0) params.minPrice = this.filters.priceRange.min;
    if (this.filters.priceRange.max < 1000) params.maxPrice = this.filters.priceRange.max;
    
    return params;
  }

  getTotalPages(): number {
    return Math.ceil(this.totalCourses / this.pageSize);
  }

  getPageNumbers(): number[] {
    const totalPages = this.getTotalPages();
    const pages = [];
    
    for (let i = 1; i <= Math.min(totalPages, 5); i++) {
      pages.push(i);
    }
    
    return pages;
  }
}