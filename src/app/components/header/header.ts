import { Component, EventEmitter, Input, Output } from '@angular/core';

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

}
