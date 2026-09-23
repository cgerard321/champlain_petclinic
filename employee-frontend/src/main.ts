//loukmane Bessam 2430635

import '@angular/localize/init';
// polyfill obligatoire, doit etre importe en premier: definit $localize globalement
// sinon Angular plante des qu'il rencontre du texte marque i18n="..." dans un template

import 'zone.js';
//for angular to see automatically the changes
import '@angular/compiler';
//compiles HTML templates
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
// contains all other components.
import { provideHttpClient } from '@angular/common/http';
// Provides HttpClient service
import { provideAnimations } from '@angular/platform-browser/animations';
// required for transitions and animations in the app.
import { loadTranslations } from '@angular/localize';
// charge une table id -> texte traduit, utilisee par $localize au premier rendu

// ngx-translate a ete retire (VETS-CPC-1927): c'est un package tiers.
// On utilise @angular/localize, le package officiel d'Angular pour le i18n.
// Le francais est la langue "source" ecrite directement dans les templates (i18n="@@id"),
// donc on a seulement besoin de charger des traductions quand la langue choisie est l'anglais.
// Si aucune traduction n'est chargee, $localize retombe sur le texte source (le francais).

async function bootstrap(): Promise<void> {
  const savedLang = localStorage.getItem('lang') ?? 'fr';

  if (savedLang === 'en') {
    try {
      const response = await fetch('/internationalization_json/en.json');
      if (!response.ok) {
        throw new Error(`Failed to load translations: ${response.status}`);
      }
      const translations = await response.json();
      loadTranslations(translations);
    } catch (err) {
      // En cas d'echec (fichier manquant, reseau, etc.), on continue en francais
      // plutot que de bloquer completement le demarrage de l'app.
      console.error('Could not load en translations, falling back to fr', err);
    }
  }

  await bootstrapApplication(AppComponent, {
    //app component inside DOM
    providers: [provideHttpClient(), provideAnimations()],
  });
}

bootstrap().catch(err => console.error(err));
//start application and .catch() catches and logs any error that occurs during bootstrap.
