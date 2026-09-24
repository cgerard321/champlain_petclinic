import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { apiBaseUrlInterceptor } from '@core/interceptors/api-base-url-interceptor';
import { errorInterceptor } from '@core/interceptors/error-interceptor';
import { authInterceptor } from '@core/interceptors/auth-interceptor';
import { AuthState } from '@core/services/auth-state';
import { firstValueFrom } from 'rxjs';
import { loadTranslations } from '@angular/localize';

registerLocaleData(localeFr);

// VETS-CPC-1927: francais = langue source des templates (i18n="@@id"), donc
// pas de fichier de traduction pour le fr. Seul l'anglais doit etre charge
// a l'execution, et ca doit se terminer AVANT que l'app ne s'affiche
// (sinon les $localize deja evalues resteraient en francais).
function getSavedLang(): string {
  return localStorage.getItem('lang') ?? 'fr';
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiBaseUrlInterceptor, errorInterceptor, authInterceptor])),
    { provide: LOCALE_ID, useValue: getSavedLang() },
    provideAppInitializer(() => {
      const authState = inject(AuthState);
      return firstValueFrom(authState.checkToken());
    }),
    provideAppInitializer(async () => {
      const lang = getSavedLang();
      document.documentElement.lang = lang;

      if (lang !== 'en') {
        return;
      }

      try {
        const response = await fetch('/i18n/en.json');
        if (!response.ok) {
          throw new Error(`Failed to load translations: ${response.status}`);
        }
        // format "simple JSON" produit par `ng extract-i18n --format=json`:
        // { "locale": "en", "translations": { id: texte, ... } }
        // loadTranslations() attend l'objet plat, pas le wrapper
        const { translations } = await response.json();
        loadTranslations(translations);
      } catch (err) {
        // En cas d'echec (fichier manquant, reseau, etc.), on continue en francais
        // plutot que de bloquer completement le demarrage de l'app.
        console.error('Could not load en translations, falling back to fr', err);
      }
    }),
  ],
};
