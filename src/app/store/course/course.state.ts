import { Course, CourseCategory, CourseFilters } from "../../models/course";

export interface CourseState {
  allCourses: Course[];
  enrolledCourses: Course[];
  trendingCourses: Course[];
  categories: CourseCategory[];
  selectedCourse: Course | null;
  searchResults: Course[];
  filters: CourseFilters;
  isLoading: boolean;
  error: string | null;
}