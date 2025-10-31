import { Component, inject, OnInit } from '@angular/core';
import { TabViewModule } from 'primeng/tabview';
import { DropdownModule } from 'primeng/dropdown';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

interface EmployeeOption {
  label: string;
  value: string;
  empId: number;
  designation: string;
}

interface TrainingOption {
  label: string;
  value: number;
  category: string;
  level: string;
}

@Component({
  selector: 'app-ro-dashboard',
  standalone: true,
  imports: [
    TabViewModule, 
    DropdownModule, 
    TableModule, 
    CardModule, 
    CommonModule, 
    FormsModule, 
    MultiSelectModule, 
    ButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './ro-dashboard.component.html',
  styleUrl: './ro-dashboard.component.scss'
})
export class RoDashboardComponent implements OnInit {
  users: EmployeeOption[] = [];
  selectedUsers: EmployeeOption[] = [];
  trainings: TrainingOption[] = [];
  selectedTrainings: TrainingOption[] = [];
  loading: boolean = false;
  currentUserId: string = '';

  private http = inject(HttpClient);
  private messageService = inject(MessageService);

  ngOnInit(): void {
    this.currentUserId = localStorage.getItem('userId') || '';
    this.loadEmployeeHierarchy();
    this.loadTrainings();
  }

  loadEmployeeHierarchy(): void {
    if (!this.currentUserId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'User ID not found'
      });
      return;
    }

    this.http.get<any>(`${environment.apiUrl}/employee-hierarchy?userId=${this.currentUserId}`)
      .subscribe({
        next: (response) => {
          this.users = response.employees.map((emp: any) => ({
            label: `${emp.empName} (${emp.emailId}) - ${emp.empDesignation}`,
            value: emp.emailId,
            empId: emp.empId,
            designation: emp.empDesignation
          }));
        },
        error: (error) => {
          console.error('Error loading employee hierarchy:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load employees in hierarchy'
          });
        }
      });
  }

  loadTrainings(): void {
    this.http.get<any[]>(`${environment.apiUrl}/courses/list-for-assignment`)
      .subscribe({
        next: (response) => {
          this.trainings = response.map((training: any) => ({
            label: `${training.trainingName} (${training.category} - ${training.level})`,
            value: training.trainingId,
            category: training.category,
            level: training.level
          }));
        },
        error: (error) => {
          console.error('Error loading trainings:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load trainings'
          });
        }
      });
  }

  assignTrainings(): void {
    if (!this.selectedUsers || this.selectedUsers.length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please select at least one user'
      });
      return;
    }

    if (!this.selectedTrainings || this.selectedTrainings.length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please select at least one training'
      });
      return;
    }

    this.loading = true;

    const requestBody = {
      userId: this.currentUserId,
      emailIdList: this.selectedUsers.map(u => u.value),
      courseIdList: this.selectedTrainings.map(t => t.value),
      enrollmentType: 'MANDATORY'
    };

    this.http.post<any[]>(`${environment.apiUrl}/enrollments/bulk-enroll`, requestBody)
      .subscribe({
        next: (response) => {
          this.loading = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: `Successfully assigned ${this.selectedTrainings.length} training(s) to ${this.selectedUsers.length} user(s)`
          });
          this.selectedUsers = [];
          this.selectedTrainings = [];
        },
        error: (error) => {
          this.loading = false;
          console.error('Error assigning trainings:', error);
          const errorMessage = error.error?.message || 'Failed to assign trainings';
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMessage
          });
        }
      });
  }
}
