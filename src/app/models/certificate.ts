export interface Certificate {
  id: string;
  userId: string;
  courseId: string;
  title: string;
  description: string;
  issuedAt: Date;
  certificateUrl: string;
  verificationCode: string;
  instructor: string;
  courseDuration: number;
  finalScore: number;
}