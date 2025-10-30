import { Component, inject, OnInit } from '@angular/core';
import { TabViewModule } from 'primeng/tabview';
import { DropdownModule } from 'primeng/dropdown';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { MetricsService } from '../../services/metrics.service';
import { CardModule } from 'primeng/card';
import { CourseService } from '../../services/course.service';
import { CalendarModule } from 'primeng/calendar';
import { FloatLabelModule } from 'primeng/floatlabel';
@Component({
  selector: 'app-lnd-admin',
  standalone: true,
  imports: [TabViewModule, DropdownModule, CalendarModule, FloatLabelModule, TableModule, CardModule, CommonModule, FormsModule, ReactiveFormsModule, MultiSelectModule, ButtonModule],
  templateUrl: './lnd-admin.html',
  styleUrl: './lnd-admin.scss'
})
export class LndAdminComponent implements OnInit {
  cities: any[] = [];
  selectedCity: any = null;
  users: any[] | undefined;
  loading: boolean = false;
  downloading: boolean = false;

  selectedUsers: any | undefined;
  trainings: any[] | undefined;

  selectedTrainings: any | undefined;

  metricsData: any[] = [];
  metricsService = inject(MetricsService);
  filterFormGroup: FormGroup | undefined;
  courseService = inject(CourseService);
  filterData: any = null;
  trainingNameList: any = [];

  ngOnInit(): void {
    this.loadForm();
    this.getCourseSearchList();

    this.users = [
      { name: 'Raman Verma', code: 'NY' },
      { name: 'Vineet Mishra', code: 'RM' },
      { name: 'Satya Prakash', code: 'LDN' },
      { name: 'Ankit Bansal', code: 'IST' },
      { name: 'Reshmi Cp', code: 'PRS' }
    ];
    this.trainings = [
      { name: 'React Js', code: 'React' },
      { name: 'Angular Js', code: 'angular' },
      { name: 'Javascript', code: 'javascript' }
    ];

    this.loadMetricsData();
  }

  loadForm() {

    this.filterFormGroup = new FormGroup({
      categoryList: new FormControl(null),
      trainingNameList: new FormControl(null),
      levelList: new FormControl(null),
      trainerNameList: new FormControl(null),
      topN: new FormControl(null),
      dateRange: new FormControl(null)
    })
  }

  getCourseSearchList() {
    this.courseService.getCourseSearchList().subscribe({
      next: (res) => {
        console.log(res);
        this.filterData = res;
        this.filterData.topN = [5, 10, 20, 50, 100];
        //this.filterData.trainingNameList = res.trainingNameList;


      }
    })
  }

  download() {
    this.downloading = false;
    const dataRange = this.filterFormGroup?.get('dateRange')?.value && this.filterFormGroup?.get('dateRange')?.value.length === 2 ? this.filterFormGroup?.get('dateRange')?.value : null
    const data = {
      category: this.filterFormGroup?.get('categoryList')?.value,
      level: this.filterFormGroup?.get('levelList')?.value,
      technology: this.filterFormGroup?.get('trainingNameList')?.value,
      startDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[0]) : null,
      endDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[1]) : null,
      topN: this.filterFormGroup?.get('topN')?.value
    };
    this.metricsService.getMetricsDownload(data).subscribe({
      next: (res) => {
        console.log(res);

        const url = window.URL.createObjectURL(res.body);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'metrics_report.xlsx'; // Replace with desired filename
        a.click();
        window.URL.revokeObjectURL(url);


        this.downloading = false;
      }, error: (err) => {
        this.downloading = false;
      }
    })

  }
  search() {
    this.loading = true;
    const dataRange = this.filterFormGroup?.get('dateRange')?.value && this.filterFormGroup?.get('dateRange')?.value.length === 2 ? this.filterFormGroup?.get('dateRange')?.value : null


    const data = {
      category: this.filterFormGroup?.get('categoryList')?.value,
      level: this.filterFormGroup?.get('levelList')?.value,
      technology: this.filterFormGroup?.get('trainingNameList')?.value,
      startDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[0]) : null,
      endDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[1]) : null,
      topN: this.filterFormGroup?.get('topN')?.value
    };
    this.metricsService.getMetrics(data).subscribe({
      next: (res) => {
        this.metricsData = res;
        this.loading = false;
      }, error: (err) => {
        this.loading = false;
      }
    })
  }

  convertDateToYYYYMMDD(dateStr: string) {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
    const formattedDate = `${year}-${month}-${day}`;
    return formattedDate;
  }

  onCategoryChange(e: any) {
    //  console.log(e);
    this.trainingNameList = this.filterData.trainingNameList.filter((x: any) => x.category === e.value).map((x: any) => x.trainingName);
    //   alert(e);
  }

  add() {

  }

  loadMetricsData() {
    this.metricsService.getMetrics({}).subscribe({
      next: (value) => {
        this.metricsData = value;
      }, error: (err) => {
      }
    })

  }


}

