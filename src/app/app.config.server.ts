import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { providePrimeNG } from 'primeng/api';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';


const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    providePrimeNG({
      ripple: true,
      cssLayer: {
          name: 'primeng',
          order: 'tailwind-base, primeng, tailwind-utilities'
      },
      theme: {
          preset: 'aura' // Or 'lara', 'material', etc.
      }
  }),
  provideAnimationsAsync()
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
