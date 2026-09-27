import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { apiBaseUrlInterceptor } from '@core/interceptors/api-base-url-interceptor';
import { authInterceptor } from '@core/interceptors/auth-interceptor';
import { errorInterceptor } from '@core/interceptors/error-interceptor';
import { AuthState } from '@core/services/auth-state';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiBaseUrlInterceptor, errorInterceptor, authInterceptor])),
    provideAppInitializer(() => {
      const authState = inject(AuthState);
      return firstValueFrom(authState.checkToken());
    }),
  ],
};
