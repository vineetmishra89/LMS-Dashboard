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
import { Globals } from '../shared/globals';


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
     globals = inject(Globals);
    filterData: any = null;
    trainingNameList: any = [];
    searchedCourse: any[] = [];

    items: any[] | undefined;

    selectedItem: any;

    suggestions: any[] | undefined;
    selectedTrainingName: any | undefined;
    selectedUserTraining: any[] = [];
    roEmailId = '';
    sbuList: string[] = [];
    projectList: string[] = [];

    ngOnInit(): void {
      const userId = localStorage.getItem('userId');
    this.roEmailId = this.globals.getUser().emailId;
      this.loadForm();
      this.getSbus();
      this.getUser();
      this.getCourseSearchList();
      
    }

    
 loadForm() {
  
  this.filterFormGroup = new FormGroup({
    selectedSbus: new FormControl([]),
    selectedProjects: new FormControl([]),
    selectedUsers: new FormControl([]),
    selectedTrainingName: new FormControl([])
  })
 }

    getSbus() {
      this.courseService.getSbusByUser(this.roEmailId).subscribe({
        next: (res) => {
          console.log('SBUs:', res);
          this.sbuList = res;
        },
        error: (err) => {
          console.error('Error fetching SBUs:', err);
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load SBUs' });
        }
      });
    }

    onSbuChange() {
      const selectedSbus = this.filterFormGroup?.get('selectedSbus')?.value;
      console.log('Selected SBUs:', selectedSbus);
      
      this.filterFormGroup?.get('selectedProjects')?.setValue([]);
      this.filterFormGroup?.get('selectedUsers')?.setValue([]);
      
      if (selectedSbus && selectedSbus.length > 0) {
        this.courseService.getProjectsByUserAndSbus(this.roEmailId, selectedSbus).subscribe({
          next: (res) => {
            console.log('Projects:', res);
            this.projectList = res;
          },
          error: (err) => {
            console.error('Error fetching projects:', err);
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects' });
          }
        });
      } else {
        this.courseService.getProjectsByUserAndSbus(this.roEmailId).subscribe({
          next: (res) => {
            console.log('Projects:', res);
            this.projectList = res;
          },
          error: (err) => {
            console.error('Error fetching projects:', err);
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects' });
          }
        });
      }
      
      this.getUser();
    }

    onProjectChange() {
      console.log('Project changed');
      this.filterFormGroup?.get('selectedUsers')?.setValue([]);
      this.getUser();
    }

    getUser() {
      const selectedProjects = this.filterFormGroup?.get('selectedProjects')?.value;
      
      const request = {
        roEmailId: this.roEmailId,
        projects: selectedProjects && selectedProjects.length > 0 ? selectedProjects : null
      };
      
      this.courseService.getEmployeesForROPMDashboard(request).subscribe({
        next: (res) => {
          console.log('Employees:', res);
          this.users = res;
        },
        error: (err) => {
          console.error('Error fetching employees:', err);
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load employees' });
        }
      });
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
      courseIdList: this.filterFormGroup?.get('selectedTrainingName')?.value.map((x: any) => x.trainingId),
      enrollmentType: 'Mandatory'
    }
    this.enrollmentService.bulkEnroll(data).subscribe({
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
