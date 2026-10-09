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

registerLocaleData(localeFr);
// save french as the first language because it is the rule in Quebec

// Returns the language the user last picked through the header switcher, falling back to the
// source locale when nothing has been stored yet.
export function getSavedLang(): string {
  return localStorage.getItem('lang') ?? 'fr';
}

export type Catalogue = { [key: string]: string | Catalogue };

//to flat the information of the en.json because the en.json have been organized: better for preventing conflicts, and for scaling the localisation in the employeee frontend
export function flattenTranslations(source: Catalogue, prefix = ''): Record<string, string> {
  const flat: Record<string, string> = {};

  for (const [key, value] of Object.entries(source)) {
    const id = prefix ? `${prefix}.${key}` : key;

    if (typeof value === 'string') {
      flat[id] = value;
    } else {
      Object.assign(flat, flattenTranslations(value, id));
    }
  }

  return flat;
}
// Installs the English translations before the first component renders.

export async function loadActiveTranslations(): Promise<void> {
  // Read the language the user last picked in the header ('fr' if nothing was saved yet).
  const lang = getSavedLang();
  document.documentElement.lang = lang;
  // Stop here to avoid a useless network call.
  if (!lang.startsWith('en')) {
    return;
  }

  try {
    //save the english file
    const response = await fetch('/i18n/en.json');
    // fetch() does not throw on HTTP errors (404, 500...), so the status must be checked by hand.
    if (!response.ok) {
      throw new Error(`Failed to load translations: ${response.status}`);
    }
    // loadTranslations() only needs the inner `translations` object, not the whole file, and it
    // needs it flat.
    const { translations } = (await response.json()) as { translations: Catalogue };
    loadTranslations(flattenTranslations(translations));
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
