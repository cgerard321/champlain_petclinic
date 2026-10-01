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

  protected readonly username = this.authState.username;

  protected readonly navBarItems: NavBarItem[] = [
    { label: 'Home', route: '/home' },
    { label: 'Veterinarians', route: '/vets' },
    { label: 'Customers', route: '/cust' },
    { label: 'Bills', route: '/bill' },
    { label: 'Visits', route: '/vist' },
    { label: 'Inventory', route: '/invt' },
    { label: 'Products', route: '/prod' },
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
