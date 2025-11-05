import { CommonModule } from '@angular/common';
import { DropdownModule } from 'primeng/dropdown';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { MetricsService } from '../../services/metrics.service';
import { CardModule } from 'primeng/card';
import { CourseService } from '../../services/course.service';
import { CalendarModule } from 'primeng/calendar';
import { FloatLabelModule } from 'primeng/floatlabel';
import { TagModule } from 'primeng/tag';
import { TabViewModule } from 'primeng/tabview';
import { Component, inject, OnInit } from '@angular/core';
import { AutoCompleteModule } from 'primeng/autocomplete';

@Component({
  selector: 'app-ro-admin',
  standalone: true,
  imports: [TabViewModule, DropdownModule, AutoCompleteModule,  TagModule , CalendarModule, FloatLabelModule, TableModule, CardModule, CommonModule, FormsModule, ReactiveFormsModule, MultiSelectModule, ButtonModule],
  templateUrl: './ro-admin.component.html',
  styleUrl: './ro-admin.component.scss'
})

export class RoAdminComponent implements OnInit {
   cities: any[] = [];
    selectedCity: any = null;
    users: any[] | undefined;
    loading: boolean = false;
    downloading: boolean = false;
    groupType: any = [];
  
    selectedUsers: any | undefined;
    selectedGroupType: any | undefined;
    trainings: any[] | undefined;
  
    selectedTrainings: any | undefined;
  
    metricsData: any[] = [];
    metricsService = inject(MetricsService);
    filterFormGroup: FormGroup | undefined;
    courseService = inject(CourseService);
    filterData: any = null;
    trainingNameList: any = [];
    searchedCourse: any[] = [];

    items: any[] | undefined;

    selectedItem: any;

    suggestions: any[] | undefined;

    ngOnInit(): void {
      this.getUser();
    }

    getUser() {
      const emailId = 'tl1@irissoftware.com';
      this.courseService.getEmployeeHierarchy(emailId).subscribe({
        next: (res) => {
          console.log(res);
          this.users = res.employees;
        }
      })
    }
    add() {

    }

    search(event: AutoCompleteCompleteEvent) {
      this.suggestions = [...Array(10).keys()].map(item => event.query + '-' + item);
  }
    
    getCourseData() {
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
        }, error: (err: Error) => {
          this.loading = false;
         // this.messageService.add({ severity: 'error', summary: 'Error', detail: err['message'] });
    
        }
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


}
interface AutoCompleteCompleteEvent {
  originalEvent: Event;
  query: string;
}