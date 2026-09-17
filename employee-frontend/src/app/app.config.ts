import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { apiBaseUrlInterceptor } from '@core/interceptors/api-base-url-interceptor';
import { errorInterceptor } from '@core/interceptors/error-interceptor';
import { authInterceptor } from '@core/interceptors/auth-interceptor';
import { AuthState } from '@core/services/auth-state';
import { firstValueFrom } from 'rxjs';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiBaseUrlInterceptor, errorInterceptor, authInterceptor])),
    provideAppInitializer(() => {
      const authState = inject(AuthState);
      return firstValueFrom(authState.checkToken());
    }),
  ]
};
