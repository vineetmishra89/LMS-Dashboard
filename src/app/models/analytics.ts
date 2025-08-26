export interface UserAnalytics {
  userId: string;
  totalCoursesEnrolled: number;
  totalCoursesCompleted: number;
  totalCertificatesEarned: number;
  totalHoursLearned: number;
  currentStreak: number;
  longestStreak: number;
  averageQuizScore: number;
  skillsAcquired: string[];
  learningPath: LearningPathProgress[];
  weeklyActivity: WeeklyActivity[];
}

export interface WeeklyActivity {
  week: string;
  hoursSpent: number;
  lessonsCompleted: number;
  quizzesAttempted: number;
}

export interface LearningPathProgress {
  pathId: string;
  pathName: string;
  progress: number;
  estimatedCompletion: Date;
}