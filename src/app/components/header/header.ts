import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrls: ['./header.scss']
})
export class HeaderComponent {

  @Input() user: any;
  @Output() mobileMenuToggle = new EventEmitter<void>();
  @Output() themeToggle = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  @Input() showMobileToggle: boolean = false;
  @Input() showLndAdmin: boolean = false;
  @Input() showRODashboard: boolean = false;
  authService = inject(AuthService);

}
