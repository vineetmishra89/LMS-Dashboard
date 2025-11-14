export const environment = {
  production: false,
  apiUrl: 'http://localhost:5000/api',
  authUrl: 'http://localhost:3000/auth',
  fileUploadUrl: 'http://localhost:3000/uploads',
  websocketUrl: 'ws://192.168.8.116:5000',
  enableDevTools: true,
  devAutoLogin: true,
  logLevel: 'debug',
  features: {
    notifications: true,
    analytics: true,
    socialLogin: true,
    videoStreaming: true,
    offlineMode: true,
    chatSupport: true,
    darkMode: true,
    multiLanguage: false,
    paymentGateway: false,
    videoConferencing: false
  }
};
