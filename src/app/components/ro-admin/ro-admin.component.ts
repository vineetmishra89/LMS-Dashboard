import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { TabPanel, TabViewModule } from 'primeng/tabview';

@Component({
  selector: 'app-ro-admin',
  standalone: true,
  imports: [ TabViewModule, CommonModule],
  templateUrl: './ro-admin.component.html',
  styleUrl: './ro-admin.component.scss'
})
export class RoAdminComponent {

}
