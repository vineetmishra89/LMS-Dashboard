import { Component, inject } from '@angular/core';
import { CarouselModule } from 'primeng/carousel';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Router } from '@angular/router';
import { CourseService } from '../../services/course.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CarouselModule, ButtonModule, TagModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  products: any[] | undefined;
  router = inject(Router);
  courseService = inject(CourseService);

  responsiveOptions: any[] | undefined;
  trendingCourses: any[] = [];
  topSearched: any[] = [];
  topRated: any[] = [];
  behavioral: any[] = [];
  technical: any[] = [];
  functional: any[] = [];

  constructor() {}

  ngOnInit() {
    this.loadHomePageCourses();

      this.responsiveOptions = [
          {
              breakpoint: '1199px',
              numVisible: 1,
              numScroll: 1
          },
          {
              breakpoint: '991px',
              numVisible: 2,
              numScroll: 1
          },
          {
              breakpoint: '767px',
              numVisible: 1,
              numScroll: 1
          }
      ];
  }

  getSeverity(status: string) {
      switch (status) {
          case 'INSTOCK':
              return 'success';
          case 'LOWSTOCK':
              return 'warning';
          case 'OUTOFSTOCK':
              return 'danger';
      }
  }

  viewCourse(product: any) {
    console.log(product);
    this.router.navigate(['/viewCourse', product.trngId]);
   //this.router.navigate(['viewCourse'])
  }

  async loadHomePageCourses() {
    this.trendingCourses = await this.getCourseCard('Enrolled', null);
    this.topSearched = await this.getCourseCard('View', null);
    this.topRated = await this.getCourseCard('Rate', null);
    this.behavioral = await this.getCourseCard('Category', 'Behavioral');
    this.technical = await this.getCourseCard('Category', 'Technical');
    this.functional = await this.getCourseCard('Category', 'Functional');
    
  }

  getCourseCard(viewType: string, category: any): Promise<any> {
    return new Promise((resolve, reject) => {
      this.courseService.getCourseCard(viewType, category).subscribe({
        next: (value) => {
          resolve(value);
        }, error: (err) => {
          reject(err);
        }
      })
    });
    
  }
}
