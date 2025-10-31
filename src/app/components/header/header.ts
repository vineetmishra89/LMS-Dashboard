import { Component, EventEmitter, inject, Input, Output, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrls: ['./header.scss']
})
export class HeaderComponent implements OnInit {

  @Input() user: any;
  @Output() mobileMenuToggle = new EventEmitter<void>();
  @Output() themeToggle = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  @Input() showMobileToggle: boolean = false;
  authService = inject(AuthService);
  private http = inject(HttpClient);
  isRo: boolean = false;

  ngOnInit(): void {
    this.checkIfRo();
  }

  checkIfRo(): void {
    const userId = localStorage.getItem('userId');
    if (userId) {
      this.http.get<{ isRo: boolean }>(`${environment.apiUrl}/employee-hierarchy/is-ro?userId=${userId}`)
        .subscribe({
          next: (response) => {
            this.isRo = response.isRo;
          },
          error: (error) => {
            console.error('Error checking RO status:', error);
            this.isRo = false;
          }
        });
    }
  }

}
