import { Component } from '@angular/core';

// VETS-CPC-1927: le footer n'existait pas du tout dans la structure layout/
// actuelle (contrairement au header/sidenav/shell). Recree ici en minimal,
// seulement pour satisfaire le critere d'acceptation #4 du ticket i18n.
@Component({
  selector: 'app-footer',
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer {}
