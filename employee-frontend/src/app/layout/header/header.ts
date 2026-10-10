import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthState } from '@core/services/auth-state';

interface NavBarItem {
  label: string;
  route: string;
}

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatMenuModule,
  ],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private readonly authState = inject(AuthState);
  private readonly router = inject(Router);

  protected currentLanguage = localStorage.getItem('lang') ?? 'fr';

  protected readonly username = this.authState.username;

  protected readonly navBarItems: NavBarItem[] = [
    { label: $localize`:@@navbar.home:Accueil`, route: '/home' },
    { label: $localize`:@@navbar.veterinarians:Vétérinaires`, route: '/vets' },
    { label: $localize`:@@navbar.customers:Clients`, route: '/cust' },
    { label: $localize`:@@navbar.bills:Factures`, route: '/bill' },
    { label: $localize`:@@navbar.visits:Visites`, route: '/vist' },
    { label: $localize`:@@navbar.inventory:Inventaire`, route: '/invt' },
    { label: $localize`:@@navbar.products:Produits`, route: '/prod' },
    { label: $localize`:@@navbar.promos:Promotions`, route: '/promo' },
  ];

  protected setting(): void {
    this.router.navigateByUrl('/settings');
  }

  protected logout(): void {
    this.authState.logout().subscribe(() => this.router.navigateByUrl('/login'));
  }

  // Saves the chosen language and reloads the page, because translations are only installed
  // at startup (see loadActiveTranslations in app.config.ts).
  protected switchLang(lang: string): void {
    // Clicking the language that is already active must not trigger a pointless reload.
    if (localStorage.getItem('lang') === lang) {
      return;
    }

    // Persist the choice so it survives the reload.
    localStorage.setItem('lang', lang);
    location.reload();
  }
}
