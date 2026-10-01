import { registerLocaleData } from '@angular/common';
//function that show how to write for a language dates numbers and money
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import localeFr from '@angular/common/locales/fr';
import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { loadTranslations } from '@angular/localize';
import { provideRouter } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { apiBaseUrlInterceptor } from '@core/interceptors/api-base-url-interceptor';
import { authInterceptor } from '@core/interceptors/auth-interceptor';
import { errorInterceptor } from '@core/interceptors/error-interceptor';
import { AuthState } from '@core/services/auth-state';

import { routes } from './app.routes';

/**
 * VETS-CPC-1927
 *
 * French is the SOURCE locale (see `angular.json` -> `i18n.sourceLocale`): French text is
 * written directly in the templates and tagged with `i18n="@@some.id"`. The templates
 * therefore already are the French version, which is why there is no French translation
 * file to load. Only English has to be fetched and installed at runtime.
 */
registerLocaleData(localeFr);
// save french as the first language because it is the rule in Quebec

// Returns the language the user last picked through the header switcher, falling back to the
// source locale when nothing has been stored yet.
export function getSavedLang(): string {
  return localStorage.getItem('lang') ?? 'fr';
}
// Installs the English translations before the first component renders.

export async function loadActiveTranslations(): Promise<void> {
  // Read the language the user last picked in the header ('fr' if nothing was saved yet).
  const lang = getSavedLang();
  document.documentElement.lang = lang;
  // Stop here to avoid a useless network call.
  if (lang !== 'en') {
    return;
  }

  try {
    //save the english file
    const response = await fetch('/i18n/en.json');
    // fetch() does not throw on HTTP errors (404, 500...), so the status must be checked by hand.
    if (!response.ok) {
      throw new Error(`Failed to load translations: ${response.status}`);
    }
    // loadTranslations() only needs the inner `translations` object, not the whole file.
    const { translations } = await response.json();
    loadTranslations(translations);
  } catch (err) {
    console.error('Could not load en translations, falling back to fr', err);
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      }),
      withInterceptors([apiBaseUrlInterceptor, errorInterceptor, authInterceptor]),
    ),
    // Drives Angular's locale-aware pipes. Kept in sync with the translations installed below.
    { provide: LOCALE_ID, useValue: getSavedLang() },
    provideAppInitializer(() => {
      const authState = inject(AuthState);
      return firstValueFrom(authState.checkToken());
    }),
    // The loader body moved out of this array into the exported `loadActiveTranslations` above
    // and is now passed by reference. Same behaviour, same ordering, but now testable.
    provideAppInitializer(loadActiveTranslations),
  ],
};
