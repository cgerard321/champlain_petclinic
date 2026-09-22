//loukmane Bessam 2430635

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

import { provideTranslateService } from '@ngx-translate/core';

import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

bootstrapApplication(AppComponent, //app component inside DOM
    {
  providers: [
    provideHttpClient(),
    provideAnimations(),
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: '/internationalization_json/',   // folder where the translation files are located
        suffix: '.json',    // file extension of the translation files
      }),
      fallbackLang: 'fr',   // language used when the is a key missing in the choosed language
      lang: 'fr',           // default active language on startup (on est au quebec !)
    }),
  ],
}).catch(err => console.error(err));
//start application and  .catch() catches and logs any error that occurs during bootstrap.