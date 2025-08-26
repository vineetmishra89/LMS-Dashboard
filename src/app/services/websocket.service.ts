import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject, timer } from 'rxjs';
import { takeUntil, retry, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { StorageService } from './storage.service';

export interface WebSocketMessage {
  type: string;
  data: any;
  timestamp: Date;
  userId?: string;
}

export interface ConnectionStatus {
  connected: boolean;
  reconnecting: boolean;
  lastConnected: Date | null;
  connectionAttempts: number;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
  private socket: WebSocket | null = null;
  private destroy$ = new Subject<void>();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 5000;

  private messagesSubject = new BehaviorSubject<WebSocketMessage | null>(null);
  private connectionStatusSubject = new BehaviorSubject<ConnectionStatus>({
    connected: false,
    reconnecting: false,
    lastConnected: null,
    connectionAttempts: 0
  });

  public messages$ = this.messagesSubject.asObservable();
  public connectionStatus$ = this.connectionStatusSubject.asObservable();

  constructor(private authService: AuthService, private storageService: StorageService) {
    this.authService.isAuthenticated$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(isAuthenticated => {
      if (isAuthenticated) {
        this.connect();
      } else {
        this.disconnect();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnect();
  }

  private connect(): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      return; // Already connected
    }

    const token = this.authService.getToken();
    const wsUrl = `${environment.websocketUrl}?token=${token}`;

    try {
      this.socket = new WebSocket(wsUrl);
      this.setupSocketEventListeners();
      this.updateConnectionStatus({ reconnecting: true, connectionAttempts: this.reconnectAttempts + 1 });
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.scheduleReconnect();
    }
  }

  private setupSocketEventListeners(): void {
    if (!this.socket) return;

    this.socket.onopen = () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
      this.updateConnectionStatus({
        connected: true,
        reconnecting: false,
        lastConnected: new Date(),
        connectionAttempts: 0
      });

      // Send authentication message
      this.sendAuthMessage();
    };

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        const message: WebSocketMessage = {
          type: data.type,
          data: data.payload,
          timestamp: new Date(data.timestamp || Date.now()),
          userId: data.userId
        };
        
        this.messagesSubject.next(message);
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error);
      }
    };

    this.socket.onclose = (event) => {
      console.log('WebSocket disconnected:', event.code, event.reason);
      this.updateConnectionStatus({ connected: false });
      
      if (!event.wasClean && this.reconnectAttempts < this.maxReconnectAttempts) {
        this.scheduleReconnect();
      }
    };

    this.socket.onerror = (error) => {
      console.error('WebSocket error:', error);
      this.updateConnectionStatus({ connected: false });
    };
  }

  private sendAuthMessage(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.sendMessage('authenticate', {
        userId: user.id,
        timestamp: new Date().toISOString()
      });
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
      return;
    }

    this.updateConnectionStatus({ reconnecting: true });
    this.reconnectAttempts++;

    timer(this.reconnectInterval * this.reconnectAttempts).pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      console.log(`Attempting WebSocket reconnection #${this.reconnectAttempts}`);
      this.connect();
    });
  }

  sendMessage(type: string, data: any): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      const message = {
        type,
        payload: data,
        timestamp: new Date().toISOString()
      };
      
      this.socket.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket not connected. Message queued for later:', { type, data });
      this.queueMessage(type, data);
    }
  }

  private queueMessage(type: string, data: any): void {
    const queuedMessages = this.storageService.getItem<any[]>('queuedMessages') || [];
    queuedMessages.push({
      type,
      data,
      timestamp: new Date().toISOString()
    });
    
    this.storageService.setItem('queuedMessages', queuedMessages);
  }

  private sendQueuedMessages(): void {
    const queuedMessages = this.storageService.getItem<any[]>('queuedMessages') || [];
    
    queuedMessages.forEach(msg => {
      this.sendMessage(msg.type, msg.data);
    });
    
    if (queuedMessages.length > 0) {
      this.storageService.removeItem('queuedMessages');
      console.log(`Sent ${queuedMessages.length} queued messages`);
    }
  }

  private disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.updateConnectionStatus({ connected: false, reconnecting: false });
  }

  private updateConnectionStatus(updates: Partial<ConnectionStatus>): void {
    const currentStatus = this.connectionStatusSubject.value;
    this.connectionStatusSubject.next({ ...currentStatus, ...updates });
  }

  // Public methods
  isConnected(): boolean {
    return this.socket?.readyState === WebSocket.OPEN;
  }

  reconnect(): void {
    this.disconnect();
    this.reconnectAttempts = 0;
    this.connect();
  }

  joinRoom(roomId: string): void {
    this.sendMessage('join_room', { roomId });
  }

  leaveRoom(roomId: string): void {
    this.sendMessage('leave_room', { roomId });
  }

  sendChatMessage(message: string, roomId?: string): void {
    this.sendMessage('chat_message', {
      message,
      roomId,
      userId: this.authService.getCurrentUser()?.id
    });
  }

  trackUserActivity(activity: string, metadata?: any): void {
    this.sendMessage('user_activity', {
      activity,
      metadata,
      timestamp: new Date().toISOString()
    });
  }
}