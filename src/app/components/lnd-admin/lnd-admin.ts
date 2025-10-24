import { Component, inject, OnInit } from '@angular/core';
import { TabViewModule } from 'primeng/tabview';
import { DropdownModule } from 'primeng/dropdown';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { MetricsService } from '../../services/metrics.service';
import { CardModule } from 'primeng/card';
@Component({
  selector: 'app-lnd-admin',
  standalone: true,
  imports: [TabViewModule , DropdownModule, TableModule, CardModule, CommonModule, FormsModule, MultiSelectModule, ButtonModule],
  templateUrl: './lnd-admin.html',
  styleUrl: './lnd-admin.scss'
})
export class LndAdminComponent implements OnInit{
  users: City[] | undefined;

    selectedUsers: City | undefined;
    trainings: City[] | undefined;

    selectedTrainings: City | undefined;

    metricsData: any[] = [];
    metricsService = inject(MetricsService);

    ngOnInit(): void {

      
      this.users = [
        { name: 'Raman Verma', code: 'NY' },
        { name: 'Vineet Mishra', code: 'RM' },
        { name: 'Satya Prakash', code: 'LDN' },
        { name: 'Ankit Bansal', code: 'IST' },
        { name: 'Reshmi Cp', code: 'PRS' }
    ];
    this.trainings = [
      { name: 'React Js', code: 'React'},
      { name: 'Angular Js', code: 'angular'},
      { name: 'Javascript', code: 'javascript'}
    ];

    this.loadMetricsData();
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
interface City {
  name: string;
  code: string;
}