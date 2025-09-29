import { Component, inject, OnInit } from '@angular/core';
import { CardModule } from 'primeng/card';
import { CarouselModule } from 'primeng/carousel';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CardModule, CarouselModule, ButtonModule, TagModule ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {

   products: any[] | undefined;
    router = inject(Router);
  
    responsiveOptions: any[] | undefined;

    selectedCourseType: string = 'Completed Course';

    ngOnInit(): void {
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
          name: 'Bamboo Watch'
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
        name: 'Bamboo Watch'
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
      name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
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
    name: 'Bamboo Watch'
    }
      ]
    
    }

    selectCard(type: string) {
      this.selectedCourseType = type;
    }

  
}
