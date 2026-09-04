import { ApplicationConfig, inject, provideAppInitializer } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { apiBaseUrlInterceptor } from '@core/interceptors/api-base-url-interceptor';
import { authInterceptor } from '@core/interceptors/auth-interceptor';
import { AuthState } from '@core/services/auth-state';
import { firstValueFrom } from 'rxjs';
import { graphqlProviders } from '@core/services/graphql-provider';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiBaseUrlInterceptor, authInterceptor])),
    ...graphqlProviders,
    provideAppInitializer(() => {
      const authState = inject(AuthState);
      return firstValueFrom(authState.checkSession());
    }),
  ],
};
