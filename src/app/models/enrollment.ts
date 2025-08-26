export interface Enrollment {
  id: string;
  userId: string;
  courseId: string;
  enrolledAt: Date;
  completedAt?: Date;
  progress: EnrollmentProgress;
  status: 'active' | 'completed' | 'dropped' | 'paused';
  certificateId?: string;
  lastAccessedAt: Date;
  timeSpent: number; // in minutes
}

export interface EnrollmentProgress {
  completedLessons: string[];
  completedModules: string[];
  currentModule: string;
  currentLesson: string;
  overallProgress: number; // 0-100
  quizScores: QuizScore[];
}

export interface QuizScore {
  quizId: string;
  score: number;
  maxScore: number;
  attemptedAt: Date;
  timeSpent: number;
}