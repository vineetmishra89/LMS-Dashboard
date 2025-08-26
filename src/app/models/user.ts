export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  profileImage: string;
  role: 'trainee' | 'instructor' | 'admin';
  preferences: UserPreferences;
  enrollmentDate: Date;
  lastActive: Date;
}

export interface UserPreferences {
  theme: 'light' | 'dark';
  language: string;
  notifications: {
    email: boolean;
    push: boolean;
    courseUpdates: boolean;
    achievements: boolean;
  };
}