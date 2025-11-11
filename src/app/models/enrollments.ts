import { CourseDetail, CourseMaster } from "./course";

export interface EnrollmentMapping {
  trainingEnrollmentId: number;
  userId: string;
  status: string;
  enrolledTs: string;
  enrolledByEmailId: string;
  startTs: string;
  enrollmentType: string;
  progressPercent: number;
  createdTs: string;
  updatedTs: string;
  createdBy: string;
  updatedBy: string;
  enrollmentDetailsList: EnrollmentDetails[];
  courseSummary: CourseMaster;
}

export interface EnrollmentDetails {
  enrollmentDetailsId: number;
  trainingEnrollmentId: number;
  moduleId: number;
  status: string;
  currentLearningTs: number;
  progressPercent: number;
  completedTs: string;
  createdTs: string;
  updatedTs: string;
  createdBy: string;
  updatedBy: string;
  courseDetail: CourseDetail;
}

export interface EnrollmentProgress {
  completedLessons: string[];
  completedModules: string[];
  currentModule: string;
  currentLesson: string;
  overallProgress: number; // 0-100
}