import { UserState } from './user/user.state';
import { CourseState } from './course/course.state';
import { EnrollmentState } from './enrollment/enrollment.state';
import { AnalyticsState } from './analytics/analytics.state';

export interface AppState {
  user: UserState;
  courses: CourseState;
  enrollments: EnrollmentState;
  analytics: AnalyticsState;
}