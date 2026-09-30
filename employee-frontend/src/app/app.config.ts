/**
 * VETS-CPC-1927
 * The two conflicting hunks were resolved by keeping both sides: main's `provideHttpClient` with
 * its new XSRF configuration, followed by this ticket's `LOCALE_ID` provider. The imports were
 * reordered to satisfy main's new `import/order` rule, which also freed `registerLocaleData` and
 * `getSavedLang` from the middle of the import block where the automatic merge had stranded them.
 */

import { registerLocaleData } from '@angular/common';
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

/**
 * Registers French locale data. Angular ships only `en-US` data by default, so the built-in
 * pipes (DatePipe, CurrencyPipe, DecimalPipe, ...) would throw "Missing locale data" as soon
 * as LOCALE_ID is set to 'fr'. This has to run at module load, before the app bootstraps.
 */
registerLocaleData(localeFr);

// CHANGE MADE FOR TESTABILITY (VETS-CPC-1927)
// `export` was added to this function. It was previously module-private, so a .spec.ts file had
// no name to import and the default-language logic could not be covered by any test at all.
// The body is untouched: no behaviour change whatsoever, only visibility.

// Returns the language the user last picked through the header switcher, falling back to the
// source locale when nothing has been stored yet.
export function getSavedLang(): string {
  return localStorage.getItem('lang') ?? 'fr';
}

// CHANGE MADE FOR TESTABILITY (VETS-CPC-1927)
// This used to be an anonymous arrow function written inline inside the `providers` array below.
// Being anonymous, it had no name to import, so the fetch and both of its error paths were
// impossible to test. It is now a named export, and `provideAppInitializer` receives it by
// reference at the bottom of this file. Passing a named function reference is exactly equivalent
// to passing an inline arrow, so runtime behaviour is unchanged - this only makes the loader
// reachable from a test file.

// Installs the English translations before the first component renders.
// Ordering is the whole point of doing this in an initializer: the Angular compiler turns
// every `i18n` attribute into a `$localize` tagged template that is evaluated the first
// time its component template runs. `loadTranslations()` only affects `$localize` calls
// evaluated *after* it, so anything rendered before this resolves would stay in French,
// permanently. Angular waits for the promise returned here before bootstrapping, which
// guarantees the correct ordering.
export async function loadActiveTranslations(): Promise<void> {
  const lang = getSavedLang();
  document.documentElement.lang = lang;

  // French needs no translation file: it is the source locale baked into the templates.
  if (lang !== 'en') {
    return;
  }

  try {
    // Plain `fetch` on purpose: HttpClient would run this through apiBaseUrlInterceptor
    // and authInterceptor, which would rewrite the URL and attach a bearer token to what
    // is really just a static asset. The path is absolute because nginx serves the app at
    // the site root in every environment (see Dockerfile and nginx.conf).
    const response = await fetch('/i18n/en.json');
    if (!response.ok) {
      throw new Error(`Failed to load translations: ${response.status}`);
    }
    // "simple JSON" shape produced by `ng extract-i18n --format=json`:
    //   { "locale": "en", "translations": { "some.id": "text", ... } }
    // loadTranslations() expects the flat map, not the wrapper object.
    const { translations } = await response.json();
    loadTranslations(translations);
  } catch (err) {
    // Degrade to French rather than block startup entirely. Worth knowing: because nginx
    // falls back to index.html for unknown paths, a missing en.json arrives here as a JSON
    // parse error rather than a 404.
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
    // CHANGE MADE FOR TESTABILITY (VETS-CPC-1927)
    // The loader body moved out of this array into the exported `loadActiveTranslations` above
    // and is now passed by reference. Same behaviour, same ordering, but now testable.
    provideAppInitializer(loadActiveTranslations),
  ],
};
