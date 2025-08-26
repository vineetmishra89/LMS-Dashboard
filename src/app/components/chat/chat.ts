import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from '../../services/user.service';
import { AnalyticsService } from '../../services/analytics.service';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';

interface ChatMessage {
  id: string;
  sender: 'ai' | 'user';
  message: string;
  timestamp: Date;
  type: 'text' | 'recommendation' | 'progress' | 'achievement';
  data?: any;
}

interface AIResponse {
  message: string;
  type: string;
  data?: any;
  suggestions?: string[];
}

@Component({
  selector: 'app-chat',
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() userId!: string;
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  private destroy$ = new Subject<void>();
  private typingSubject = new BehaviorSubject<string>('');
  
  messages: ChatMessage[] = [];
  newMessage: string = '';
  isTyping: boolean = false;
  isAITyping: boolean = false;
  suggestions: string[] = [];
  currentStreak$ = new BehaviorSubject<number>(0);
  
  // Quick action buttons
  quickActions = [
    { text: 'My Progress', action: 'progress' },
    { text: 'Recommendations', action: 'recommendations' },
    { text: 'Next Lesson', action: 'next-lesson' },
    { text: 'Study Plan', action: 'study-plan' }
  ];

  constructor(
    private userService: UserService,
    private analyticsService: AnalyticsService,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService
  ) {}

  ngOnInit(): void {
    this.initializeChat();
    this.setupTypingIndicator();
    this.loadCurrentStreak();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private initializeChat(): void {
    // Load chat history or start with welcome message
    this.loadChatHistory();
    
    if (this.messages.length === 0) {
      this.addWelcomeMessage();
    }
  }

  private setupTypingIndicator(): void {
    this.typingSubject.pipe(
      debounceTime(1000),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.isTyping = false;
    });
  }

  private loadChatHistory(): void {
    // Load from localStorage or API
    const savedMessages = localStorage.getItem(`chat-${this.userId}`);
    if (savedMessages) {
      this.messages = JSON.parse(savedMessages).map((msg: any) => ({
        ...msg,
        timestamp: new Date(msg.timestamp)
      }));
    }
  }

  private saveChatHistory(): void {
    localStorage.setItem(`chat-${this.userId}`, JSON.stringify(this.messages));
  }

  private loadCurrentStreak(): void {
    if (this.userId) {
      this.analyticsService.getLearningStreak(this.userId).subscribe(streak => {
        this.currentStreak$.next(streak.current);
      });
    }
  }

  private addWelcomeMessage(): void {
    const user = this.userService.getCurrentUser();
    const welcomeMessage: ChatMessage = {
      id: this.generateId(),
      sender: 'ai',
      message: `Hi ${user?.firstName || 'there'}! 👋 I'm your AI learning assistant. I can help you with course recommendations, track your progress, answer questions about your learning path, and much more. What would you like to know?`,
      timestamp: new Date(),
      type: 'text'
    };
    
    this.messages.push(welcomeMessage);
    this.generateSuggestions();
    this.saveChatHistory();
  }

  private generateSuggestions(): void {
    this.suggestions = [
      "What's my current progress?",
      "Recommend courses for me",
      "When is my next assignment due?",
      "Show my learning analytics",
      "What skills should I focus on?"
    ];
  }

  sendMessage(message?: string): void {
    const messageText = message || this.newMessage.trim();
    if (!messageText) return;

    const userMessage: ChatMessage = {
      id: this.generateId(),
      sender: 'user',
      message: messageText,
      timestamp: new Date(),
      type: 'text'
    };

    this.messages.push(userMessage);
    this.newMessage = '';
    this.isAITyping = true;

    // Process AI response
    this.processAIResponse(messageText);
    this.saveChatHistory();
  }

  private processAIResponse(userMessage: string): void {
    const typingDelay = 1000 + Math.random() * 2000; // 1-3 seconds
    
    setTimeout(() => {
      this.getAIResponse(userMessage).then(response => {
        const aiMessage: ChatMessage = {
          id: this.generateId(),
          sender: 'ai',
          message: response.message,
          timestamp: new Date(),
          type: response.type as any,
          data: response.data
        };

        this.messages.push(aiMessage);
        this.isAITyping = false;
        
        if (response.suggestions) {
          this.suggestions = response.suggestions;
        }
        
        this.saveChatHistory();
      }).catch(error => {
        console.error('AI response error:', error);
        
        const errorMessage: ChatMessage = {
          id: this.generateId(),
          sender: 'ai',
          message: "I apologize, but I'm having trouble processing your request right now. Please try again in a moment.",
          timestamp: new Date(),
          type: 'text'
        };
        
        this.messages.push(errorMessage);
        this.isAITyping = false;
        this.saveChatHistory();
      });
    }, typingDelay);
  }

  private async getAIResponse(message: string): Promise<AIResponse> {
    const lowerMessage = message.toLowerCase();
    
    try {
      // Progress inquiry
      if (lowerMessage.includes('progress') || lowerMessage.includes('how am i doing')) {
        const analytics = await this.analyticsService.getUserAnalytics(this.userId).toPromise();
        const enrollments = await this.enrollmentService.getUserEnrollments(this.userId).toPromise();
        
        const activeEnrollments = enrollments?.filter(e => e.status === 'active') || [];
        const completedEnrollments = enrollments?.filter(e => e.status === 'completed') || [];
        
        return {
          message: `Great question! Here's your current progress:\n\n📚 **Courses:**\n• ${analytics?.totalCoursesEnrolled || 0} courses enrolled\n• ${activeEnrollments.length} currently active\n• ${completedEnrollments.length} completed\n\n⏱️ **Study Time:**\n• ${analytics?.totalHoursLearned || 0} total hours learned\n• ${Math.round((analytics?.totalHoursLearned || 0) / 7)} hours per week average\n\n🔥 **Streak:**\n• ${analytics?.currentStreak || 0} day current streak\n• ${analytics?.longestStreak || 0} day longest streak\n\n🎯 **Performance:**\n• ${analytics?.averageQuizScore || 0}% average quiz score\n• ${analytics?.skillsAcquired?.length || 0} skills acquired\n\nYou're doing amazing! Keep up the great work! 🎉`,
          type: 'progress',
          data: analytics,
          suggestions: ['Show detailed analytics', 'Set study goals', 'View certificates']
        };
      }

      // Recommendations
      if (lowerMessage.includes('recommend') || lowerMessage.includes('suggest') || lowerMessage.includes('what should i learn')) {
        const recommendations = await this.analyticsService.getRecommendations(this.userId).toPromise();
        const trendingCourses = await this.courseService.getTrendingCourses(3).toPromise();
        
        let responseMessage = "Based on your learning history and goals, here are my recommendations:\n\n";
        
        if (recommendations && recommendations.length > 0) {
          responseMessage += "📚 **Personalized for You:**\n";
          recommendations.slice(0, 3).forEach((rec: any, i: number) => {
            responseMessage += `${i + 1}. **${rec.title}** - ${rec.reason}\n`;
          });
        }
        
        if (trendingCourses && trendingCourses.length > 0) {
          responseMessage += "\n🔥 **Trending Courses:**\n";
          trendingCourses.forEach((course: any, i: number) => {
            responseMessage += `${i + 1}. **${course.title}** by ${course.instructor.firstName} ${course.instructor.lastName}\n`;
          });
        }
        
        return {
          message: responseMessage,
          type: 'recommendation',
          data: { recommendations, trendingCourses },
          suggestions: ['Enroll in course', 'View course details', 'More recommendations']
        };
      }

      // Next lesson inquiry
      if (lowerMessage.includes('next lesson') || lowerMessage.includes('what should i study') || lowerMessage.includes('continue learning')) {
        const enrollments = await this.enrollmentService.getUserEnrollments(this.userId, { 
          status: ['active'], 
          includeCompleted: false 
        }).toPromise();
        
        if (enrollments && enrollments.length > 0) {
          // Get most recently accessed course
          const activeEnrollment = enrollments.sort((a, b) => 
            new Date(b.lastAccessedAt).getTime() - new Date(a.lastAccessedAt).getTime()
          )[0];
          
          const course = await this.courseService.getCourseById(activeEnrollment.courseId).toPromise();
          const progressPercent = activeEnrollment.progress.overallProgress;
          
          return {
            message: `Your next lesson is in **"${course?.title}"**:\n\n📖 **Current:** ${activeEnrollment.progress.currentLesson || 'Getting Started'}\n📚 **Module:** ${activeEnrollment.progress.currentModule || 'Introduction'}\n📊 **Progress:** ${progressPercent}% complete\n⏱️ **Time Spent:** ${Math.round(activeEnrollment.timeSpent / 60)} hours\n\nReady to continue learning? Let's keep that momentum going! 💪`,
            type: 'text',
            data: { enrollment: activeEnrollment, course },
            suggestions: ['Continue this lesson', 'Show course details', 'Set study reminder']
          };
        } else {
          return {
            message: `You don't have any active courses right now. Would you like me to recommend some courses to get started? 🚀\n\nI can suggest courses based on:\n• Popular topics in your field\n• Trending courses this week\n• Courses that match your skill level\n• Learning paths for career growth`,
            type: 'text',
            suggestions: ['Show recommendations', 'Browse popular courses', 'Find courses by topic']
          };
        }
      }

      // Analytics and detailed stats
      if (lowerMessage.includes('analytics') || lowerMessage.includes('stats') || lowerMessage.includes('detailed progress')) {
        const analytics = await this.analyticsService.getUserAnalytics(this.userId).toPromise();
        const weeklyActivity = analytics?.weeklyActivity || [];
        
        let activitySummary = '';
        if (weeklyActivity.length > 0) {
          const lastWeek = weeklyActivity[weeklyActivity.length - 1];
          activitySummary = `\n📈 **This Week:**\n• ${lastWeek.hoursSpent || 0} hours studied\n• ${lastWeek.lessonsCompleted || 0} lessons completed\n• ${lastWeek.quizzesAttempted || 0} quizzes attempted`;
        }
        
        return {
          message: `Here's your detailed learning analytics:\n\n📊 **Overall Performance:**\n• **Completion Rate:** ${analytics?.totalCoursesCompleted || 0}/${analytics?.totalCoursesEnrolled || 0} courses (${Math.round(((analytics?.totalCoursesCompleted || 0) / (analytics?.totalCoursesEnrolled || 1)) * 100)}%)\n• **Average Quiz Score:** ${analytics?.averageQuizScore || 0}%\n• **Skills Acquired:** ${analytics?.skillsAcquired?.length || 0}\n\n🔥 **Learning Streak:**\n• **Current Streak:** ${analytics?.currentStreak || 0} days\n• **Longest Streak:** ${analytics?.longestStreak || 0} days\n\n⏰ **Study Time:**\n• **Total Hours:** ${analytics?.totalHoursLearned || 0} hours\n• **Daily Average:** ${Math.round((analytics?.totalHoursLearned || 0) / 30)} hours${activitySummary}\n\n🎯 **Achievements:**\n• **Certificates Earned:** ${analytics?.totalCertificatesEarned || 0}\n• **Learning Paths:** ${analytics?.learningPath?.length || 0} in progress`,
          type: 'text',
          data: analytics,
          suggestions: ['Export progress report', 'Set study goals', 'View certificates']
        };
      }

      // Study schedule and reminders
      if (lowerMessage.includes('schedule') || lowerMessage.includes('reminder') || lowerMessage.includes('study plan')) {
        const upcomingDeadlines = await this.enrollmentService.getUpcomingDeadlines(this.userId).toPromise();
        
        let scheduleMessage = "📅 **Your Study Schedule:**\n\n";
        
        if (upcomingDeadlines && upcomingDeadlines.length > 0) {
          scheduleMessage += "⚠️ **Upcoming Deadlines:**\n";
          upcomingDeadlines.forEach((deadline: any, i: number) => {
            const daysLeft = Math.ceil((new Date(deadline.dueDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24));
            scheduleMessage += `${i + 1}. **${deadline.title}** - ${daysLeft} days left\n`;
          });
          scheduleMessage += "\n";
        }
        
        scheduleMessage += "💡 **Study Recommendations:**\n";
        scheduleMessage += "• Study 30-45 minutes daily for best retention\n";
        scheduleMessage += "• Take breaks every 25 minutes (Pomodoro technique)\n";
        scheduleMessage += "• Review previous lessons before starting new ones\n";
        scheduleMessage += "• Set consistent study times each day\n\n";
        scheduleMessage += "Would you like me to help you create a personalized study schedule?";
        
        return {
          message: scheduleMessage,
          type: 'text',
          data: { upcomingDeadlines },
          suggestions: ['Create study schedule', 'Set daily reminders', 'View calendar']
        };
      }

      // Course specific questions
      if (lowerMessage.includes('course') && (lowerMessage.includes('which') || lowerMessage.includes('what'))) {
        const enrolledCourses = await this.courseService.getEnrolledCourses(this.userId).toPromise();
        
        if (enrolledCourses && enrolledCourses.length > 0) {
          let coursesList = "📚 **Your Enrolled Courses:**\n\n";
          
          for (let i = 0; i < enrolledCourses.length; i++) {
            const course = enrolledCourses[i];
            const enrollment = await this.enrollmentService.getUserEnrollments(this.userId).toPromise();
            const courseEnrollment = enrollment?.find(e => e.courseId === course.id);
            const progress = courseEnrollment?.progress.overallProgress || 0;
            
            coursesList += `${i + 1}. **${course.title}**\n`;
            coursesList += `   • Instructor: ${course.instructor.firstName} ${course.instructor.lastName}\n`;
            coursesList += `   • Progress: ${progress}% complete\n`;
            coursesList += `   • Status: ${courseEnrollment?.status || 'unknown'}\n\n`;
          }
          
          return {
            message: coursesList,
            type: 'text',
            data: { enrolledCourses },
            suggestions: ['Continue a course', 'View course details', 'Browse new courses']
          };
        } else {
          return {
            message: "You're not currently enrolled in any courses. Would you like me to help you find some great courses to start your learning journey? 🚀",
            type: 'text',
            suggestions: ['Browse courses', 'Show recommendations', 'Popular courses']
          };
        }
      }

      // Achievements and certificates
      if (lowerMessage.includes('certificate') || lowerMessage.includes('achievement') || lowerMessage.includes('badge')) {
        const analytics = await this.analyticsService.getUserAnalytics(this.userId).toPromise();
        
        return {
          message: `🏆 **Your Achievements:**\n\n🎓 **Certificates Earned:** ${analytics?.totalCertificatesEarned || 0}\n🔥 **Current Streak:** ${analytics?.currentStreak || 0} days\n📈 **Skills Mastered:** ${analytics?.skillsAcquired?.length || 0}\n\n${analytics?.totalCertificatesEarned ? 'Congratulations on your achievements! You can view and download your certificates from the Certificates page.' : 'Keep learning to earn your first certificate! Complete a course to get started.'}`,
          type: 'achievement',
          data: analytics,
          suggestions: ['View certificates', 'Share achievements', 'Continue learning']
        };
      }

      // Help and instructions
      if (lowerMessage.includes('help') || lowerMessage.includes('how') || lowerMessage.includes('can you')) {
        return {
          message: `I'm here to help! Here's what I can do for you:\n\n📊 **Progress Tracking:**\n• Show your current progress in all courses\n• Calculate completion rates and study time\n• Track your learning streak\n\n🎯 **Recommendations:**\n• Suggest courses based on your interests\n• Recommend study schedules\n• Identify skill gaps\n\n📚 **Course Support:**\n• Answer questions about course content\n• Help with study planning\n• Provide learning tips and strategies\n\n🏆 **Achievements:**\n• Track your certificates and badges\n• Celebrate learning milestones\n• Share your progress\n\nJust ask me anything about your learning journey!`,
          type: 'text',
          suggestions: ['Show my progress', 'Recommend courses', 'Study tips', 'View achievements']
        };
      }

      // Default responses with context
      const defaultResponses = [
        `That's an interesting question! I'm here to help with your learning journey. ${this.getContextualHelp()}`,
        `I understand you're looking for assistance. ${this.getContextualHelp()}`,
        `Thanks for reaching out! ${this.getContextualHelp()}`,
        `I'm here to support your educational goals! ${this.getContextualHelp()}`
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
        type: 'text',
        suggestions: ['Try again', 'Contact support', 'Browse courses']
      };
    }
  }

  private getContextualHelp(): string {
    const user = this.userService.getCurrentUser();
    const timeOfDay = new Date().getHours();
    
    if (timeOfDay < 12) {
      return `Good morning${user?.firstName ? ` ${user.firstName}` : ''}! What would you like to focus on today?`;
    } else if (timeOfDay < 17) {
      return `Good afternoon${user?.firstName ? ` ${user.firstName}` : ''}! How can I help with your learning goals?`;
    } else {
      return `Good evening${user?.firstName ? ` ${user.firstName}` : ''}! Ready for some evening study time?`;
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

  onSuggestionClick(suggestion: string): void {
    this.sendMessage(suggestion);
  }

  onTyping(): void {
    this.isTyping = true;
    this.typingSubject.next(this.newMessage);
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        const element = this.messagesContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
    } catch (err) {
      console.warn('Could not scroll to bottom:', err);
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

  exportChatHistory(): void {
    const chatData = {
      userId: this.userId,
      messages: this.messages,
      exportedAt: new Date().toISOString()
    };
    
    const dataStr = JSON.stringify(chatData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = `chat-history-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    
    URL.revokeObjectURL(url);
  }

  // Helper methods for template
  trackByMessageId(index: number, message: ChatMessage): string {
    return message.id;
  }

  formatMessage(message: string): string {
    // Convert markdown-style formatting to HTML
    return message
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/\n/g, '<br>')
      .replace(/• /g, '&bull; ');
  }

  getStreakMessage(streak: number): string {
    if (streak === 0) return "Start your streak today!";
    if (streak < 3) return "Keep it up!";
    if (streak < 7) return "You're on fire! 🔥";
    if (streak < 30) return "Amazing dedication! 🎯";
    return "Learning legend! 🏆";
  }

  getStreakDots(): { day: string; active: boolean }[] {
    const today = new Date();
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const result = [];
    const currentStreak = this.currentStreak$.value;
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(today.getDate() - i);
      result.push({
        day: days[date.getDay()],
        active: i <= currentStreak - 1
      });
    }
    
    return result;
  }

  showStreakInfo(): void {
  const infoMessage: ChatMessage = {
    id: this.generateId(),
    sender: 'ai',
    message: `🔥 **Learning Streaks Explained:**\n\nA learning streak counts consecutive days you've engaged with course content. Here's how it works:\n\n✅ **Streak Requirements:**\n• Complete at least one lesson\n• Watch a video for 5+ minutes\n• Take a quiz or assignment\n• Spend 15+ minutes studying\n\n🎯 **Streak Benefits:**\n• Builds consistent learning habits\n• Unlocks achievement badges\n• Improves knowledge retention\n• Boosts your learning profile\n\n💡 **Pro Tips:**\n• Study at the same time daily\n• Set realistic daily goals\n• Use short study sessions\n• Don't break the chain!\n\nKeep up your amazing streak! 🚀`,
    timestamp: new Date(),
    type: 'text'
  };
  
  this.messages.push(infoMessage);
  this.saveChatHistory();
}

getNextMilestone(currentStreak: number): number {
  const milestones = [7, 14, 30, 60, 90, 180, 365];
  return milestones.find(m => m > currentStreak) || currentStreak + 30;
}

getMilestoneProgress(currentStreak: number): number {
  const nextMilestone = this.getNextMilestone(currentStreak);
  const previousMilestone = currentStreak >= 7 ? 
    [0, 7, 14, 30, 60, 90, 180].reverse().find(m => m <= currentStreak) || 0 : 0;
  
  return ((currentStreak - previousMilestone) / (nextMilestone - previousMilestone)) * 100;
}

// Format duration helper
formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const mins = Math.floor(minutes % 60);
  
  if (hours > 0) {
    return `${hours}h ${mins}m`;
  }
  return `${mins}m`;
}
}