import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';

@Component({
  selector: 'app-play-course',
  standalone: true,
  imports: [InputTextModule, InputTextareaModule, CommonModule, FormsModule, ButtonModule  ],
  templateUrl: './play-course.component.html',
  styleUrl: './play-course.component.scss'
})
export class PlayCourseComponent {
  value: string = '';
  videoUrl: string = '/assets/my-intro-video.mp4';
  playlist = [
    {name: 'Day 1 - Introduction', duration: '30 mins'}, 
    {name: 'Day 2 - Setup & Configuration', duration: '40 mins'},
    {name: 'Day 3 - Module 1', duration: '40 mins'},
    {name: 'Day 4 - Module 2', duration: '40 mins'},
    {name: 'Day 5 - Module 3', duration: '40 mins'},
    {name: 'Day 6 - Module 4', duration: '40 mins'},
    {name: 'Day 7 - Module 5', duration: '40 mins'},
    {name: 'Day 8 - Module 6', duration: '40 mins'},
  ];

  forumDiscussion: any[] = [{user: 'Ankit Bansal', ratings: 3, when: '3 weeks ago', comments: 'This course is good for intermediate level. Instructor explained topics very well'},
    {user: 'Vineet Mishra', ratings: 3, when: '1 day ago', comments: 'The instructor is taking a right approach teaching concepts very much to the point, crisp with practical applications and time limit of each lesson.'},
    {user: 'Satya Prakash Mishra',  ratings: 3, when: 'Today', comments: 'Great Learning so far. Very Clear and detailed information with all resources available.'},
    {user: 'Rajib Bhattacharya',  ratings: 3, when: '3 months ago', comments: 'Learnt the basics! Thank you so much.'}
  ]
}
