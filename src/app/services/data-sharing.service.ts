import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root' // Makes the service a singleton across the application
})
export class DataSharingService {
  private dataSubject = new BehaviorSubject<any>(null); // Initialize with a default value
  public sharedData$: Observable<any> = this.dataSubject.asObservable();

  constructor() { }

  sendData(data: any) {
    this.dataSubject.next(data); // Push new data to subscribers
  }

  getData(): Observable<any> {
    return this.sharedData$; // Allow components to subscribe
  }

  
}