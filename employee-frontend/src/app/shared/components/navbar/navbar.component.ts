//Loukmane Bessam 2430635

import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  switchLang(lang: string): void {
    // on sauvegarde le choix puis on recharge la page pour que main.ts
    // recharge les bonnes traductions AVANT que l'app ne re-bootstrap
    localStorage.setItem('lang', lang);
    location.reload();
  }
}
