import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { VideoPlayerComponent } from '../video-player/video-player.component';
import { ActivatedRoute } from '@angular/router';
import { DataSharingService } from '../../services/data-sharing.service';

@Component({
  selector: 'app-play-course',
  standalone: true,
  imports: [InputTextModule, InputTextareaModule, CommonModule, FormsModule, ButtonModule  ],
  templateUrl: './play-course.component.html',
  styleUrl: './play-course.component.scss'
})
export class PlayCourseComponent implements OnInit {
  value: string = '';
  trainingId: string = '0';
  route = inject(ActivatedRoute);
  dataSharingService = inject(DataSharingService);
  playCourseData: any = null;
  videoUrl: string = 'https://irissoft-my.sharepoint.com/personal/learning_devel_irissoftware_com/_layouts/15/stream.aspx?id=%2Fpersonal%2Flearning%5Fdevel%5Firissoftware%5Fcom%2FDocuments%2FRecordings%5FAll%2FTraining%20Recordings%2FiElevate%204%5FJava%5FSep%2725%5FSBU1%2FiElevate%5FBusiness%20Need%20Training%20%2D%20Java%20%2015th%20Sep%20%E2%80%93%2026th%20Sep%5F945am%2D1130am%2D20250915%5F095127%2DMeeting%20Recording%2Emp4';
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

  ngOnInit(): void {
    this.trainingId = this.route.snapshot.paramMap.get('trainingId') || '0';
    this.dataSharingService.getData().subscribe(data => {
      this.playCourseData = data;
    });
  }
}
