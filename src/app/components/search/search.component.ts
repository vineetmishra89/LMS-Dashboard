import { Component, inject, OnInit } from '@angular/core';
import { CardModule } from 'primeng/card';
import { CarouselModule } from 'primeng/carousel';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Router } from '@angular/router';
import { ProgressBarModule } from 'primeng/progressbar';

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
  
    responsiveOptions: any[] | undefined;

    selectedCourseType: string = 'Completed Course';

    ngOnInit(): void {
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

    selectCard(type: string) {
      this.selectedCourseType = type;
    }

  
}
