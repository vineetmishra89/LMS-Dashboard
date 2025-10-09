import { Component, inject } from '@angular/core';
import { CarouselModule } from 'primeng/carousel';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Router } from '@angular/router';

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

  responsiveOptions: any[] | undefined;

  constructor() {}

  ngOnInit() {
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

  viewCourse() {
    this.router.navigate(['viewCourse'])
  }
}
