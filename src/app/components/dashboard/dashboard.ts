

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable, combineLatest, Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, map, startWith, catchError, switchMap, distinctUntilChanged, debounceTime, take, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User } from '../../models/user';
import { Enrollment } from '../../models/enrollment';
import { UserService } from '../../services/user.service';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { AnalyticsService } from '../../services/analytics.service';
import { VideoProgressService } from '../../services/video-progress.service';
import { CourseDetail, CourseMaster } from '../../models/course';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
interface City {
  name: string;
  code: string;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
  standalone: true,
  imports: [CommonModule, ButtonModule, DropdownModule, ReactiveFormsModule, FormsModule]
})
export class DashboardComponent implements OnInit, OnDestroy {

  loading: boolean = false;
  cities: City[] | undefined;
  selectedCity: City | undefined;
  private destroy$ = new Subject<void>();
  isLoading$ = new BehaviorSubject<boolean>(true);
  hasError$ = new BehaviorSubject<string | null>(null);

  currentUser$: Observable<User | null>;
  dashboardData$!: Observable<any>;
  private latestVm: any = null;
  catalog$!: Observable<any[]>;
  catalogCopy$!: Observable<any[]>;
  isFilterOperation: boolean = false;
  pendingFilters = { category: '', topic: '', instructor: '', level: '' };
  private appliedFilters$ = new BehaviorSubject<{
    category: string; topic: string; instructor: string;
  }>(this.pendingFilters);

  /** Track enrolled masters in-memory (mirror your real enrollment state if you have it) */
  private enrolledIds = new Set<string>();
  enrolledIds$ = new BehaviorSubject<Set<string>>(this.enrolledIds);

  /** Modal state */
  showDetails = false;
  selectedMaster: CourseMaster | null = null;

  displayedColumns = [
    'trainingName','description','topics','level','instructorName',
    'duration','category','prerequisite','toolsNeeded','reviewComments','action'
  ] as const;

  responsiveOptions: any[] | undefined;
  filterData: any = null;
  filterFormGroup: FormGroup | undefined;
  selectedCategory: any = null;
  selectedTrainingName: any = null;
  selectedLevel: any = null;
  selectedTrainerName: any = null;
  trainingNameList: any = [];
  searchedCourse: any = [];

  constructor(
    private router: Router,
    private userService: UserService,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private analyticsService: AnalyticsService,
    private videoProgressService: VideoProgressService
  ) {
    this.currentUser$ = this.userService.currentUser$;

    this.cities = [
      { name: 'New York', code: 'NY' },
      { name: 'Rome', code: 'RM' },
      { name: 'London', code: 'LDN' },
      { name: 'Istanbul', code: 'IST' },
      { name: 'Paris', code: 'PRS' }
  ];
  }

  ngOnInit(): void {
   
   
    this.getCourseSearchList();
    this.loadForm();
 }

 loadForm() {
  
  this.filterFormGroup = new FormGroup({
    categoryList: new FormControl(null),
    trainingNameList: new FormControl(null),
    levelList: new FormControl(null),
    trainerNameList: new FormControl(null),
  })
 }

  getCourseSearchList() {
    this.courseService.getCourseSearchList().subscribe({
      next: (res) => {
        console.log(res);
        this.filterData = res;
        //this.filterData.trainingNameList = res.trainingNameList;
       
        
      }
    })
  }

  viewCourse(course: any) {
    console.log(course);
    
    this.router.navigate(['/viewCourse', 1]);
    //this.router.navigate(['/viewCourse', course.trainingId]);
   //this.router.navigate(['viewCourse'])
  }

  search() {
    this.loading = true;
    const data = {
      category: this.filterFormGroup?.get('categoryList')?.value,
      topic: this.filterFormGroup?.get('trainingNameList')?.value,
      instructor: this.filterFormGroup?.get('trainerNameList')?.value,
      level: this.filterFormGroup?.get('levelList')?.value

    }
    this.courseService.getCourseDetail(data).subscribe({
      next: (res) => {
        this.searchedCourse = res;
        this.loading = false;
      }, error: (err) => {
        this.loading = false;
      }
    })
  }

  onCategoryChange(e: any) {
  //  console.log(e);
    this.trainingNameList = this.filterData.trainingNameList.filter((x: any) => x.category === e.value).map((x: any) => x.trainingName);
 //   alert(e);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }



}
