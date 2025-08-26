import { Injectable } from '@angular/core';
import { Observable, from } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  
  // Local Storage with JSON support
  setItem(key: string, value: any): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('Failed to save to localStorage:', error);
    }
  }

  getItem<T>(key: string): T | null {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.error('Failed to read from localStorage:', error);
      return null;
    }
  }

  removeItem(key: string): void {
    localStorage.removeItem(key);
  }

  clear(): void {
    localStorage.clear();
  }

  // Session Storage
  setSessionItem(key: string, value: any): void {
    try {
      sessionStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('Failed to save to sessionStorage:', error);
    }
  }

  getSessionItem<T>(key: string): T | null {
    try {
      const item = sessionStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.error('Failed to read from sessionStorage:', error);
      return null;
    }
  }

  // IndexedDB for large data (offline support)
  async setIndexedDBItem(storeName: string, key: string, value: any): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('LMSDatabase', 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        const db = request.result;
        const transaction = db.transaction([storeName], 'readwrite');
        const store = transaction.objectStore(storeName);
        const putRequest = store.put(value, key);
        
        putRequest.onsuccess = () => resolve();
        putRequest.onerror = () => reject(putRequest.error);
      };
      
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains(storeName)) {
          db.createObjectStore(storeName);
        }
      };
    });
  }

  async getIndexedDBItem<T>(storeName: string, key: string): Promise<T | null> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('LMSDatabase', 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        const db = request.result;
        const transaction = db.transaction([storeName], 'readonly');
        const store = transaction.objectStore(storeName);
        const getRequest = store.get(key);
        
        getRequest.onsuccess = () => resolve(getRequest.result || null);
        getRequest.onerror = () => reject(getRequest.error);
      };
    });
  }
}