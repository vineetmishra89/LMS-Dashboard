export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api',
  authUrl: 'http://localhost:3000/auth',
  fileUploadUrl: 'http://localhost:3000/uploads',
  websocketUrl: 'ws://localhost:3000',
  features: {
    notifications: true,
    analytics: true,
    socialLogin: true,
    videoStreaming: true,
    offlineMode: false
  }
};