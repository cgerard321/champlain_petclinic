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

// ngx-translate a ete retire (VETS-CPC-1927): c'est un package tiers.
// On repart avec @angular/localize, le package officiel d'Angular pour le i18n.

bootstrapApplication(AppComponent, //app component inside DOM
    {
  providers: [
    provideHttpClient(),
    provideAnimations(),
  ],
}).catch(err => console.error(err));
//start application and  .catch() catches and logs any error that occurs during bootstrap.
