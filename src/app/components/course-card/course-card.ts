import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Course } from '../../models/course';
import { Enrollment } from '../../models/enrollment';
import { EnrollmentService } from '../../services/enrollment.service';
import { CourseService } from '../../services/course.service';
import { AnalyticsService } from '../../services/analytics.service';

type ChatMessage = { id: string; message?: string };

@Component({
  selector: 'app-course-card',
  templateUrl: './course-card.html',
  styleUrls: ['./course-card.scss']
})
export class CourseCardComponent implements OnInit {
  @Input() course!: Course;
  @Input() enrollment?: Enrollment;
  @Input() showEnrollButton: boolean = false;
  @Input() showProgress: boolean = true;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() userId!: string;
  
  @Output() enrollClick = new EventEmitter<string>();
  @Output() continueClick = new EventEmitter<string>();
  @Output() viewDetails = new EventEmitter<string>();

  isEnrolling: boolean = false;
  progressPercentage: number = 0;
  timeRemaining: string = '';
  isTyping: boolean = false;
  typingSubject: any;
  messagesContainer: any;
  messages: ChatMessage[] = [];

  constructor(private enrollmentService: EnrollmentService,
    private courseService: CourseService,private analyticsService: AnalyticsService
  ) {}

  ngOnInit(): void {
    this.calculateProgress();
    this.calculateTimeRemaining();
  }

  private calculateProgress(): void {
    if (this.enrollment) {
      this.progressPercentage = this.enrollment.progress.overallProgress;
    }
  }

  private calculateTimeRemaining(): void {
    if (this.course.duration && this.enrollment) {
      const completedTime = (this.course.duration * this.progressPercentage) / 100;
      const remaining = this.course.duration - completedTime;
      this.timeRemaining = this.formatDuration(remaining);
    } else if (this.course.duration) {
      this.timeRemaining = this.formatDuration(this.course.duration);
    }
  }

  private formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = Math.floor(minutes % 60);
    
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  }

  async onEnroll(): Promise<any> {
    if (!this.isEnrolling) {
      this.isEnrolling = true;
      this.enrollClick.emit(this.course.id);
      
      try {
        // Next lesson inquiry
        const userMessage = ''; // TODO: Assign the actual user message here
        const lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.includes('next lesson') || lowerMessage.includes('what should i study')) {
          const enrollments = await this.enrollmentService.getUserEnrollments(this.userId).toPromise();
          const activeEnrollment = enrollments?.find(e => e.status === 'active' && e.progress.overallProgress < 100);
          
          if (activeEnrollment) {
            const course = await this.courseService.getCourseById(activeEnrollment.courseId).toPromise();
            return {
              message: `Your next lesson is in "${course?.title}":\n\n📖 ${activeEnrollment.progress.currentLesson || 'Getting Started'}\n📚 Module: ${activeEnrollment.progress.currentModule || 'Introduction'}\n\nReady to continue learning? 💪`,
              type: 'text',
              suggestions: ['Continue this lesson', 'Show course details', 'Set study reminder']
            };
          } else {
            return {
              message: `You don't have any active courses right now. Would you like me to recommend some courses to get started? 🚀`,
              type: 'text',
              suggestions: ['Show recommendations', 'Browse popular courses', 'Find courses by topic']
            };
          }
        }

        // Analytics inquiry
        if (lowerMessage.includes('analytics') || lowerMessage.includes('stats')) {
          const analytics = await this.analyticsService.getUserAnalytics(this.userId).toPromise();
          return {
            message: `Here's your detailed learning analytics:\n\n📊 **Performance:**\n• Average quiz score: ${analytics?.averageQuizScore || 0}%\n• Skills acquired: ${analytics?.skillsAcquired?.length || 0}\n• Longest streak: ${analytics?.longestStreak || 0} days\n\n📈 **Activity:**\n• Total study time: ${analytics?.totalHoursLearned || 0} hours\n• Courses completed: ${analytics?.totalCoursesCompleted || 0}\n• Certificates earned: ${analytics?.totalCertificatesEarned || 0}`,
            type: 'text',
            data: analytics
          };
        }

        // Default responses
        const defaultResponses = [
          "That's a great question! I'm here to help with your learning journey. Could you be more specific about what you'd like to know?",
          "I understand you're looking for help. I can assist with course progress, recommendations, scheduling, and answering questions about your learning path.",
          "Thanks for reaching out! I'm designed to support your educational goals. What specific area would you like help with today?",
          "I'm here to make your learning experience better! Whether it's tracking progress, finding new courses, or getting study tips, I'm ready to help."
        ];

        return {
          message: defaultResponses[Math.floor(Math.random() * defaultResponses.length)],
          type: 'text',
          suggestions: [
            "What's my progress?",
            "Recommend courses",
            "Study tips",
            "Set study goals"
          ]
        };
      } catch (error) {
        console.error('Error getting AI response:', error);
        return {
          message: "I apologize, but I'm having trouble accessing your data right now. Please try again in a moment, or feel free to ask me something else!",
          type: 'text'
        };
      }
    }
  }

  onQuickAction(action: string): void {
    switch (action) {
      case 'progress':
        this.sendMessage("What's my current progress?");
        break;
      case 'recommendations':
        this.sendMessage("Can you recommend some courses for me?");
        break;
      case 'next-lesson':
        this.sendMessage("What should I study next?");
        break;
      case 'study-plan':
        this.sendMessage("Help me create a study plan");
        break;
    }
  }
  sendMessage(arg0: string) {
    throw new Error('Method not implemented.');
  }

  onSuggestionClick(suggestion: string): void {
    this.sendMessage(suggestion);
  }

  onTyping(): void {
    this.isTyping = true;
    this.typingSubject.next(this.newMessage);
  }
  newMessage(newMessage: any) {
    throw new Error('Method not implemented.');
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  private generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  clearChat(): void {
    this.messages = [];
    this.addWelcomeMessage();
    this.saveChatHistory();
  }
  addWelcomeMessage() {
    throw new Error('Method not implemented.');
  }
  saveChatHistory() {
    throw new Error('Method not implemented.');
  }

  trackByMessageId(index: number, message: ChatMessage): string {
  return message.id;
}

formatMessage(message: string): string {
  // Convert markdown-style formatting to HTML
  return message
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>');
}

getStreakMessage(streak: number): string {
  if (streak === 0) return "Start your streak today!";
  if (streak < 3) return "Keep it up!";
  if (streak < 7) return "You're on fire! 🔥";
  if (streak < 30) return "Amazing dedication! 🎯";
  return "Learning legend! 🏆";
}

  getStreakDots(): string[] {
  const today = new Date();
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const result = [];
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(today.getDate() - i);
    result.push(days[date.getDay()]);
  }
  
  return result;
} 


  onContinue(): void {
    this.continueClick.emit(this.course.id);
  }

  onViewDetails(): void {
    this.viewDetails.emit(this.course.id);
  }

    getStatusText(): string {
    if (!this.enrollment) return 'Not Enrolled';
    
    switch (this.enrollment.status) {
      case 'active':
        return this.progressPercentage === 100 ? 'Completed' : 'In Progress';
      case 'completed':
        return 'Completed';
      case 'paused':
        return 'Paused';
      case 'dropped':
        return 'Dropped';
      default:
        return 'Unknown';
    }
  }

    getProgressColor(): string {
    if (this.progressPercentage >= 90) return '#10b981'; // green
    if (this.progressPercentage >= 60) return '#3b82f6'; // blue
    if (this.progressPercentage >= 30) return '#f59e0b'; // yellow
    return '#ef4444'; // red
  }
}
