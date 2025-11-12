import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseMaster } from '../../models/course';
import { CourseService } from '../../services/course.service';
import { UserService } from '../../services/user.service';
import { DataSharingService } from '../../services/data-sharing.service';
import { CommonModule } from '@angular/common';
import { Globals } from '../../components/shared/globals';

@Component({
  selector: 'app-video-player-page',
  templateUrl: './video-player-page.component.html',
  styleUrls: ['./video-player-page.component.scss']
})
export class VideoPlayerPageComponent implements OnInit {
  course: CourseMaster | null = null;
  isLoading = true;
   dataSharingService = inject(DataSharingService);
   globals = inject(Globals);
   playCourseData: any = null;
   selectedModule: any = null;
   
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private userService: UserService
  ) {}
  
  ngOnInit(): void {
    const courseId = this.route.snapshot.params['trainingId'];
    const userId = this.globals.getUser().emailId;
    
    const navigationState = this.router.getCurrentNavigation()?.extras?.state || 
                           (history.state && history.state.courseData ? history.state : null);
    
    if (navigationState && navigationState.courseData) {
      console.log('Using course data from navigation state:', navigationState.courseData);
      this.course = navigationState.courseData;
      this.isLoading = false;
    } else {
      this.courseService.getCourseById(courseId,userId).subscribe({
        next: (course) => {
          this.course = course;
          console.log('First course lession id- '+ this.course.lmsTrainingDetails[0].moduleId);
          this.isLoading = false;
          this.selectedModule = course.lmsTrainingDetails[0];
        },
        error: (error) => {
          console.error('Failed to load course:', error);
          this.router.navigate(['/dashboard']);
        }
      });
    }

    this.dataSharingService.getData().subscribe(data => {
      this.playCourseData = data;
    });
  }

  moduleSelected(selectedModule: any, index: number) {
    this.selectedModule = selectedModule

  }
  
  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
