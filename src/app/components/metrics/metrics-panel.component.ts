import { Component, inject, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { CourseService } from '../../services/course.service';

@Component({
  selector: 'app-metrics-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DropdownModule,
    CalendarModule,
    ButtonModule,
    TableModule,
    CardModule
  ],
  templateUrl: './metrics-panel.component.html',
  styleUrl: './metrics-panel.component.scss'
})
export class MetricsPanelComponent implements OnInit {
  @Input() metricsService: any;
  @Input() mode: 'org' | 'hierarchy' = 'org';

  loading: boolean = false;
  downloading: boolean = false;
  metricsData: any = {};
  filterFormGroup: FormGroup | undefined;
  courseService = inject(CourseService);
  filterData: any = null;
  trainingNameList: any = [];

  ngOnInit(): void {
    this.loadForm();
    this.getCourseSearchList();
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
    });
  }

  getCourseSearchList() {
    this.courseService.getCourseSearchList().subscribe({
      next: (res) => {
        this.filterData = res;
        this.filterData.topN = [5, 10, 20, 50, 100];
      }
    });
  }

  onCategoryChange(e: any) {
    this.trainingNameList = this.filterData.trainingNameList
      .filter((x: any) => x.category === e.value)
      .map((x: any) => x.trainingName);
  }

  search() {
    this.loading = true;
    const dataRange = this.filterFormGroup?.get('dateRange')?.value && 
                      this.filterFormGroup?.get('dateRange')?.value.length === 2 
                      ? this.filterFormGroup?.get('dateRange')?.value 
                      : null;

    const data = {
      category: this.filterFormGroup?.get('categoryList')?.value,
      level: this.filterFormGroup?.get('levelList')?.value,
      technology: this.filterFormGroup?.get('trainingNameList')?.value,
      startDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[0]) : null,
      endDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[1]) : null,
      topN: this.filterFormGroup?.get('topN')?.value
    };

    this.metricsService.getMetrics(data).subscribe({
      next: (res: any) => {
        this.metricsData = res;
        this.loading = false;
      },
      error: (err: any) => {
        this.loading = false;
      }
    });
  }

  download() {
    this.downloading = true;
    const dataRange = this.filterFormGroup?.get('dateRange')?.value && 
                      this.filterFormGroup?.get('dateRange')?.value.length === 2 
                      ? this.filterFormGroup?.get('dateRange')?.value 
                      : null;

    const data = {
      category: this.filterFormGroup?.get('categoryList')?.value,
      level: this.filterFormGroup?.get('levelList')?.value,
      technology: this.filterFormGroup?.get('trainingNameList')?.value,
      startDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[0]) : null,
      endDate: dataRange ? this.convertDateToYYYYMMDD(dataRange[1]) : null,
      topN: this.filterFormGroup?.get('topN')?.value
    };

    this.metricsService.getMetricsDownload(data).subscribe({
      next: (res: any) => {
        const url = window.URL.createObjectURL(res.body);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.mode === 'hierarchy' ? 'hierarchy_metrics_report.xlsx' : 'metrics_report.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        this.downloading = false;
      },
      error: (err: any) => {
        this.downloading = false;
      }
    });
  }

  loadMetricsData() {
    this.metricsService.getMetrics({}).subscribe({
      next: (value: any) => {
        this.metricsData = value;
      },
      error: (err: any) => {
        console.error('Error loading metrics:', err);
      }
    });
  }

  convertDateToYYYYMMDD(dateStr: string) {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;
    return formattedDate;
  }
}
