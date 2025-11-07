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
import { EnrollmentService } from '../../services/enrollment.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { PickListModule } from 'primeng/picklist';


@Component({
  selector: 'app-ro-admin',
  standalone: true,
  imports: [TabViewModule , ToastModule, PickListModule, DropdownModule, AutoCompleteModule, ProgressSpinnerModule, TagModule , CalendarModule, FloatLabelModule, TableModule, CardModule, CommonModule, FormsModule, ReactiveFormsModule, MultiSelectModule, ButtonModule],
  templateUrl: './ro-admin.component.html',
  styleUrl: './ro-admin.component.scss',
    providers: [MessageService, ConfirmationService]
})

export class RoAdminComponent implements OnInit {
   cities: any[] = [];
    selectedCity: any = null;
    users: any[] | undefined = [];
    loading: boolean = false;
    downloading: boolean = false;
    groupType: any = [];
  
    selectedUsers: any[] | undefined = [];
    selectedGroupType: any | undefined;
    trainings: any[] | undefined;
  
    selectedTrainings: any | undefined;
  
    metricsData: any[] = [];
    metricsService = inject(MetricsService);
    filterFormGroup: FormGroup | undefined;
    courseService = inject(CourseService);
     enrollmentService = inject(EnrollmentService);
     messageService = inject(MessageService);
     confirmationService = inject(ConfirmationService);
    filterData: any = null;
    trainingNameList: any = [];
    searchedCourse: any[] = [];

    items: any[] | undefined;

    selectedItem: any;

    suggestions: any[] | undefined;
    selectedTrainingName: any | undefined;
    selectedUserTraining: any[] = [];
    roEmailId = 'rajib.bhattacharya@irissoftware.com';

    ngOnInit(): void {
      this.loadForm();
      this.getUser();
      this.getCourseSearchList();
      
    }

    
 loadForm() {
  
  this.filterFormGroup = new FormGroup({
    selectedUsers: new FormControl([]),
    selectedTrainingName: new FormControl([])
  })
 }

    getUser() {
      const emailId = this.roEmailId;
      this.courseService.getEmployeeHierarchy(emailId).subscribe({
        next: (res) => {
          console.log(res);
          this.users = res.employees;
        }
      })
    }

    add() {
      if (this.filterFormGroup?.get('selectedUsers')?.value?.length === 0) {
        this.messageService.add({ severity: 'warn', summary: '', detail: 'Please select employee' });
        return;
      }if (this.filterFormGroup?.get('selectedTrainingName')?.value?.length == 0) {
        this.messageService.add({ severity: 'warn', summary: '', detail: 'Please select training name' });
        return;
      }

      this.selectedUserTraining = this.filterFormGroup?.get('selectedUsers')?.value;
    }

    search(event: AutoCompleteCompleteEvent) {
      this.suggestions = [...Array(10).keys()].map(item => event.query + '-' + item);
  }

  bulkEnroll() {
    this.loading = true;
    
    const data = {
      userId: this.roEmailId,
      emailIdList: this.filterFormGroup?.get('selectedUsers')?.value.map((x: any) => x.emailId),
      courseIdList: this.filterFormGroup?.get('selectedTrainingName')?.value.map((x: any) => x.trainingName),
      enrollmentType: 'Mandatory'
    }
    this.enrollmentService.enroll(data).subscribe({
      next :(res) => {
        this.loading = false;
        this.filterFormGroup?.reset();
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Successful' });

      }, error: (err: Error) => {
        this.loading = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.message });
      }
    });
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