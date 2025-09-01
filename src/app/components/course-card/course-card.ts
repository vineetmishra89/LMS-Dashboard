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
    if (this.enrollment?.progress?.overallProgress !== undefined) {
      this.progressPercentage = this.enrollment.progress.overallProgress;
    } else {
      this.progressPercentage = 0;
    }
  }

  private calculateTimeRemaining(): void {
    if (this.course.durationMinutes && this.enrollment) {
      const completedTime = (this.course.durationMinutes * this.progressPercentage) / 100;
      const remaining = this.course.durationMinutes - completedTime;
      this.timeRemaining = this.formatDuration(remaining);
    } else if (this.course.durationMinutes) {
      this.timeRemaining = this.formatDuration(this.course.durationMinutes);
    }
  }

  private formatDuration(minutes: number): string {
    const minute = Math.floor(minutes / 60);
    const sec = Math.floor(minutes % 60);
    
    if (minute > 0) {
      return `${minute}m ${sec}s`;
    }
    return `${sec}m`;
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

  onWatchVideo(): void {
    if (this.course.videoUrl) {
      window.open(this.course.videoUrl, '_blank');
    }
  }

  getDefaultThumbnail(category: string): string {
    const defaultThumbnails: { [key: string]: string } = {
      'Technology': 'https://images.unsplash.com/photo-1518709268805-4e9042af2176?w=400&h=300&fit=crop&auto=format',
      'Education': 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=300&fit=crop&auto=format',
      'Cloud': 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=400&h=300&fit=crop&auto=format',
      'Java': 'https://images.unsplash.com/photo-1517077304055-6e89abbf09b0?w=400&h=300&fit=crop&auto=format',
      'Programming': 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=300&fit=crop&auto=format',
      'Web Development': 'https://images.unsplash.com/photo-1547658719-da2b51169166?w=400&h=300&fit=crop&auto=format',
      'Data Science': 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400&h=300&fit=crop&auto=format'
    };
    
    return defaultThumbnails[category] || defaultThumbnails['Technology'];
  }

  getThumbnailUrl(): string {
    //return this.course.thumbnail || this.getDefaultThumbnail(this.course.category || 'Technology');
    return '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';
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
