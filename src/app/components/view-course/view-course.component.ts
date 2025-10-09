import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Button, ButtonModule } from "primeng/button";
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-view-course',
  standalone: true,
  imports: [ButtonModule, OverlayPanelModule, InputGroupModule,InputGroupAddonModule  ],
  templateUrl: './view-course.component.html',
  styleUrl: './view-course.component.scss'
})
export class ViewCourseComponent {

  router = inject(Router);
  liked: boolean = false;

  hasEnrolled: boolean = false;
  courseRatings: any[] = [{user: 'Ankit Bansal', ratings: 3, when: '3 weeks ago', comments: 'This course is good for intermediate level. Instructor explained topics very well'},
    {user: 'Vineet Mishra', ratings: 3, when: '1 day ago', comments: 'The instructor is taking a right approach teaching concepts very much to the point, crisp with practical applications and time limit of each lesson.'},
    {user: 'Satya Prakash Mishra',  ratings: 3, when: 'Today', comments: 'Great Learning so far. Very Clear and detailed information with all resources available.'},
    {user: 'Rajib Bhattacharya',  ratings: 3, when: '3 months ago', comments: 'Learnt the basics! Thank you so much.'}
  ]

  enroll() {
    this.hasEnrolled = true;
  }

  resume() {
    this.router.navigate(['runningCourse']);
  }

}
