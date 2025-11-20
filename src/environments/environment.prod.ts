export const environment = {
  production: true,
  apiUrl: 'http://192.168.8.116:5000/api',
  authUrl: 'https://api.yourlms.com/auth',
  fileUploadUrl: 'https://api.yourlms.com/uploads',
  websocketUrl: 'wss://api.yourlms.com',
  features: {
    notifications: true,
    analytics: true,
    socialLogin: true,
    videoStreaming: true,
    offlineMode: true
  }
};
