import { ApplicationConfig, inject, provideAppInitializer } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { apiBaseUrlInterceptor } from '@core/interceptors/api-base-url-interceptor';
import { authInterceptor } from '@core/interceptors/auth-interceptor';
import { AuthStateService } from '@core/services/auth-state-service';
import { firstValueFrom } from 'rxjs';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiBaseUrlInterceptor, authInterceptor])),
    provideAppInitializer(() => {
      const authState = inject(AuthStateService);
      return firstValueFrom(authState.checkSession());
    }),
  ],
};
