// src/app/components/shared/not-found.component.ts
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.scss']
})
export class NotFoundComponent implements OnInit {
  attemptedUrl: string = '';
  suggestedPages = [
    { name: 'Dashboard', path: '/dashboard', icon: '📊', description: 'Your learning overview' },
    { name: 'Browse Courses', path: '/courses', icon: '📚', description: 'Explore our course catalog' },
    { name: 'My Certificates', path: '/certificates', icon: '🏆', description: 'View your achievements' },
    { name: 'Profile Settings', path: '/profile', icon: '👤', description: 'Manage your account' }
  ];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.attemptedUrl = this.router.url;
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goBack(): void {
    this.location.back();
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  searchCourses(): void {
    this.router.navigate(['/courses'], { 
      queryParams: { search: this.getSearchTermFromUrl() } 
    });
  }

  reportBrokenLink(): void {
    // In a real app, this would send feedback to your backend
    console.log('Reporting broken link:', this.attemptedUrl);
    alert('Thank you for reporting this broken link. Our team will investigate.');
  }

  private getSearchTermFromUrl(): string {
    const urlParts = this.attemptedUrl.split('/');
    return urlParts[urlParts.length - 1] || '';
  }
}
