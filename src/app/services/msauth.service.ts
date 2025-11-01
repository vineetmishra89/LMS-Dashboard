
import { Injectable } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { AuthenticationResult } from '@azure/msal-browser';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { loginRequest } from './../auth.config';

@Injectable({
  providedIn: 'root'
})
export class MSAuthService {
  token:String = '';
  constructor(private msalService: MsalService) {}

  login(): Observable<AuthenticationResult | null> {
    // Check if we're in an iframe
    const isIframe = window !== window.parent && !window.opener;
    
    if (isIframe) {
      // Use popup for iframes
      return this.msalService.loginPopup(loginRequest);
    } else {
      // Use redirect for standalone windows
      this.msalService.loginRedirect(loginRequest);
      return new Observable(observer => {
        observer.next(null);
        observer.complete();
      });
    }
  }

  logout(): void {
    this.msalService.logout();
  }

  getToken(){
    return this.token;
  }
  setToken(token:String){
    this.token = token;
  }

  getActiveAccount() {
    return this.msalService.instance.getActiveAccount();
  }

  getAccessToken(): Observable<AuthenticationResult> {
    const account = this.getActiveAccount();
    if (account) {
      const accessTokenRequest = {
        scopes: ['User.Read'],
        account: account
      };
      return this.msalService.acquireTokenSilent(accessTokenRequest);
    } else {
      return new Observable(observer => {
        observer.error(new Error('No active account'));
      });
    }
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getActiveAccount() != null;
  }

 
}
