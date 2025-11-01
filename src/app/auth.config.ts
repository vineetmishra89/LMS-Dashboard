
import { Configuration, PublicClientApplication } from '@azure/msal-browser';

export const msalConfig: Configuration = {
  auth: {
    clientId: '62189f96-20ec-42b7-8fff-bceafe77afd5', // Replace with your Azure AD app registration client ID
    authority: 'https://login.microsoftonline.com/73bffe2b-9041-4754-aaf0-3ef61cde7559', // Replace with your tenant ID
    redirectUri: (typeof window !== 'undefined' && window?.location?.origin) || 'http://localhost:4200' // Use current origin
  },
  cache: {
    cacheLocation: 'localStorage',
    storeAuthStateInCookie: false
  }
};

export const loginRequest = {
  scopes: ['openid', 'profile', 'User.Read','Files.Read.All']
};

export const msalInstance = new PublicClientApplication(msalConfig);

// Initialize MSAL instance only in browser
if (typeof window !== 'undefined') {
  msalInstance.initialize().then(() => {
    // Handle the result of initialize
    const activeAccount = msalInstance.getActiveAccount();
    if (!activeAccount && msalInstance.getAllAccounts().length > 0) {
      msalInstance.setActiveAccount(msalInstance.getAllAccounts()[0]);
    }
  });
}
