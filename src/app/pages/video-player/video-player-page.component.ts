import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Course } from '../../models/course';
import { CourseService } from '../../services/course.service';

@Component({
  selector: 'app-video-player-page',
  templateUrl: './video-player-page.component.html',
  styleUrls: ['./video-player-page.component.scss']
})
export class VideoPlayerPageComponent implements OnInit {
  course: Course | null = null;
  isLoading = true;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService
  ) {}
  
  ngOnInit(): void {
    const courseId = this.route.snapshot.params['courseId'];
    
    const navigationState = this.router.getCurrentNavigation()?.extras?.state || 
                           (history.state && history.state.courseData ? history.state : null);
    
    if (navigationState && navigationState.courseData) {
      console.log('Using course data from navigation state:', navigationState.courseData);
      this.course = navigationState.courseData;
      this.isLoading = false;
    } else {
      this.courseService.getCourseById(courseId).subscribe({
        next: (course) => {
          this.course = course;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Failed to load course:', error);
          this.router.navigate(['/dashboard']);
        }
      });
    }
  }
  
  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
