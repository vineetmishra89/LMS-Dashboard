export interface Course {
  id: string;
  trainingName: string;
  outline: string;
  title: string;
  category: string;
  topics: string;
  instructorName: string;
  durationMinutes: number;
  trainerRating: number;
  userRating: number;
  description?: string;
  instructor?: Instructor;
  level?: 'beginner' | 'intermediate' | 'advanced';
  reviewComments: string[];
  prerequisite: string;
  toolsNeeded: string;
  duration?: number;
  rating?: number;
  reviewCount?: number;
  enrollmentCount?: number;
  price?: number;
  currency?: string;
  videoUrl?: string;
  modules?: CourseModule[];
  tags?: string[];
  isPublished?: boolean;
  isTrending?: boolean;
}

export interface CourseModule {
  id: string;
  title: string;
  description: string;
  orderIndex: number;
  duration: number;
  lessons: Lesson[];
  isCompleted?: boolean;
}

export interface Lesson {
  id: string;
  title: string;
  type: 'video' | 'text' | 'quiz' | 'assignment';
  content: string;
  videoUrl?: string;
  duration: number;
  orderIndex: number;
  isCompleted?: boolean;
  completedAt?: Date;
}

export interface Instructor {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  bio: string;
  profileImage: string;
  expertise: string[];
  rating: number;
  totalStudents: number;
  totalCourses: number;
}

export interface CourseCategory {
  id: string;
  name: string;
  description: string;
  icon: string;
  parentId?: string;
}

export interface CourseFilters {
  category: string[];
  level: string[];
  priceRange: { min: number; max: number };
  rating: number;
  duration: { min: number; max: number };
  instructor: string[];
}
