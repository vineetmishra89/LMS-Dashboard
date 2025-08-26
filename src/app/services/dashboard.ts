import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Course, Stats, Certificate } from '../models/course';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor() { }

  getStats(): Observable<Stats> {
    return of({
      enrolledCourses: 12,
      certificates: 8,
      hoursLearned: 156,
      usersOnline: 2341
    });
  }

  getCurrentCourse(): Observable<Course> {
    return of({
      id: '1',
      title: 'Advanced Machine Learning',
      instructor: 'Dr. Alex Chen',
      rating: 4.8,
      reviews: 2341,
      progress: 78,
      module: 'Module 4: Neural Networks',
      image: 'assets/images/ml-course.jpg'
    });
  }

  getMyCourses(): Observable<Course[]> {
    return of([
      {
        id: '2',
        title: 'Python for Data Science',
        instructor: 'Prof. Maria Rodriguez',
        rating: 4.9,
        reviews: 1500,
        progress: 92,
        module: 'Progress',
        image: 'assets/images/python-course.jpg'
      },
      {
        id: '3',
        title: 'Deep Learning Fundamentals',
        instructor: 'Dr. James Wilson',
        rating: 4.7,
        reviews: 980,
        progress: 45,
        module: 'Progress',
        image: 'assets/images/deep-learning-course.jpg'
      }
    ]);
  }

  getTrendingCourses(): Observable<Course[]> {
    return of([
      {
        id: '4',
        title: 'Building AI Chatbots',
        instructor: 'Sarah Johnson',
        rating: 4.8,
        reviews: 750,
        progress: 0,
        module: '',
        image: 'assets/images/chatbot-course.jpg',
        duration: '12 hours',
        trending: true
      },
      {
        id: '5',
        title: 'Computer Vision Mastery',
        instructor: 'Dr. Lisa Park',
        rating: 4.9,
        reviews: 1200,
        progress: 0,
        module: '',
        image: 'assets/images/cv-course.jpg',
        duration: '18 hours',
        trending: true
      }
    ]);
  }

  getCertificates(): Observable<Certificate[]> {
    return of([
      {
        title: 'Python Basics',
        completedDate: 'Dec 2024',
        icon: '🎓'
      },
      {
        title: 'Data Analysis',
        completedDate: 'Nov 2024',
        icon: '🔵'
      }
    ]);
  }
}