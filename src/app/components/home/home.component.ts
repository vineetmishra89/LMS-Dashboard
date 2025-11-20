import { Component, inject } from '@angular/core';
import { CarouselModule } from 'primeng/carousel';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Router } from '@angular/router';
import { CourseService } from '../../services/course.service';
import { DataSharingService } from '../../services/data-sharing.service';
import { Globals } from '../../core/globals';

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
  dataSharingService = inject(DataSharingService);
  globals = inject(Globals);

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
    this.recordSearchHistory(product);
    product.trainerDetail = {
      names: product.trainerNames, 
      emails: product.trainerEmailIds
    }
    this.dataSharingService.sendData(product.trainerDetail);
    this.router.navigate(['/viewCourse', product.trngId]);
   //this.router.navigate(['viewCourse'])
  }

  recordSearchHistory(course: any) {
    const data = {
      trng_id: course.trngId,
      email_id: this.globals.getUser().emailId
      }
    this.courseService.recordSearchHistory(data).subscribe({
      next: (res) => {
        console.log('res', res);
      }, error: (err) => {
        console.log('Error', err);
      }
    })

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
