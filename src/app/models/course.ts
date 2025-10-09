export interface CourseDetail {
  trainingDetailId: string;
  trainingId: string;
  trainersCurrentFeedback?: number;
  currentUserFeedback?: number;
  instructorName?: string;
  topic?: string;
  duration?: number;
  trainingLink?: string;
}

export interface CourseMaster {
  trainingId: string;
  trainingName: string;                 // Training Name
  description?: string;
  topics?: string;
  level?: string;
  content?: string;
  instructorName?: string;
  duration?: number;       // or minutes if that’s what you store
  category?: string;
  details: CourseDetail[];
  reviewComments?: string;
  prerequisite?: string;
  toolsNeeded?: string;
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
