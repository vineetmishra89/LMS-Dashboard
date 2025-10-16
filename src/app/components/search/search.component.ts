import { Component, inject, OnInit } from '@angular/core';
import { CardModule } from 'primeng/card';
import { CarouselModule } from 'primeng/carousel';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Router } from '@angular/router';
import { ProgressBarModule } from 'primeng/progressbar';
import { CourseService } from '../../services/course.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CardModule, CarouselModule, ButtonModule, TagModule, ProgressBarModule  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {

   products: any[] | undefined;
   completed: any[] = [];
   pending: any[] = [];
   enrolled: any[] = [];
    router = inject(Router);
    courseService = inject(CourseService);
  
    responsiveOptions: any[] | undefined;

    selectedCourseType: string = 'Completed Course';
    summary: any;
    userId = 'trainee2@example.com';
    enrolledCourse: any = [];
    pendingCourse: any = [];
    completedCourse: any = [];
    likedCourse: any = [];

    ngOnInit(): void {
      this.getSummary();
      this.getCompletedCourse();
      this.getEnrolledCourse();
      this.getLikedCourses();
      this.getPendingCourses();
      
      

      this.enrolled = [
        {
          id: '1000',
          category: 'Technology',
          details: [1,2,3,4],
          level: 'Beginner',
          duration: '10 Hours',
          instructorName: 'Vineet Mishra',
          description: 'klsdjfkldsjfd',
          trainingName: 'React JS',
          name: 'Bamboo Watch',
          progress: 0
      },
      {
        id: '1000',
        category: 'Technology',
        details: [1,2,3,4],
        level: 'Beginner',
        duration: '10 Hours',
        instructorName: 'Vineet Mishra',
        description: 'klsdjfkldsjfd',
        trainingName: 'React JS',
        name: 'Bamboo Watch',
        progress: 0
    },
    
      ]
      this.pending = [
        {
          id: '1000',
          category: 'Technology',
          details: [1,2,3,4],
          level: 'Beginner',
          duration: '10 Hours',
          instructorName: 'Vineet Mishra',
          description: 'klsdjfkldsjfd',
          trainingName: 'React JS',
          name: 'Bamboo Watch',
          progress: 20
      },
      {
        id: '1000',
        category: 'Technology',
        details: [1,2,3,4],
        level: 'Beginner',
        duration: '10 Hours',
        instructorName: 'Vineet Mishra',
        description: 'klsdjfkldsjfd',
        trainingName: 'React JS',
        name: 'Bamboo Watch',
        progress: 80
    },
    
      ]
      this.completed = [
        {
          id: '1000',
          category: 'Technology',
          details: [1,2,3,4],
          level: 'Beginner',
          duration: '10 Hours',
          instructorName: 'Vineet Mishra',
          description: 'klsdjfkldsjfd',
          trainingName: 'React JS',
         
          progress: 100
      },
      {
        id: '1000',
        category: 'Technology',
        details: [1,2,3,4],
        level: 'Beginner',
        duration: '10 Hours',
        instructorName: 'Vineet Mishra',
        description: 'klsdjfkldsjfd',
        trainingName: 'React JS',
        name: 'Bamboo Watch',
        progress: 100
    },
    {
      id: '1000',
      category: 'Technology',
      details: [1,2,3,4],
      level: 'Beginner',
      duration: '10 Hours',
      instructorName: 'Vineet Mishra',
      description: 'klsdjfkldsjfd',
      trainingName: 'React JS',
      name: 'Bamboo Watch',
      progress: 100
    },
    
      ];
      this.products = [
        {
          id: '1000',
          category: 'Technology',
          details: [1,2,3,4],
          level: 'Beginner',
          duration: '10 Hours',
          instructorName: 'Vineet Mishra',
          description: 'klsdjfkldsjfd',
          trainingName: 'React JS',
          name: 'Bamboo Watch',
          progress: 0
      },
      {
        id: '1000',
        category: 'Technology',
        details: [1,2,3,4],
        level: 'Beginner',
        duration: '10 Hours',
        instructorName: 'Vineet Mishra',
        description: 'klsdjfkldsjfd',
        trainingName: 'React JS',
        name: 'Bamboo Watch',
        progress: 0
    },
    {
      id: '1000',
      category: 'Technology',
      details: [1,2,3,4],
      level: 'Beginner',
      duration: '10 Hours',
      instructorName: 'Vineet Mishra',
      description: 'klsdjfkldsjfd',
      trainingName: 'React JS',
      name: 'Bamboo Watch',
      progress: 0
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 20
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 20
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 80
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 80
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 100
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 100
    },
    {
    id: '1000',
    category: 'Technology',
    details: [1,2,3,4],
    level: 'Beginner',
    duration: '10 Hours',
    instructorName: 'Vineet Mishra',
    description: 'klsdjfkldsjfd',
    trainingName: 'React JS',
    name: 'Bamboo Watch',
    progress: 100
    }
      ]
    
    }

    getSummary() {
      this.courseService.getSummary(this.userId).subscribe(res => {
        console.log('summar', res);
        this.summary = res;
      }, err => {
        console.log(err);
      })
    }
      getEnrolledCourse() {
        this.courseService.getEnrolledCourse(this.userId).subscribe({
          next: (res) => {
            this.enrolledCourse = res;
          }
        })
      }
      getCompletedCourse() {
        this.courseService.getCompletedCourse(this.userId).subscribe({
          next: (res) => {
            this.completedCourse = res;
          }
        })
      }
      getPendingCourses() {
        this.courseService.getPendingCourses(this.userId).subscribe({
          next: (res) => {
            this.pendingCourse = res;
          }
        })
      }
      getLikedCourses() {
        this.courseService.getPendingCourses(this.userId).subscribe({
          next: (res) => {
            this.likedCourse = res;
          }
        })
      }
    
    selectCard(type: string) {
      this.selectedCourseType = type;
    }

    start(selectedCourse: any) {
      
        this.router.navigate(['runningCourse']);
      
    }

  
}
