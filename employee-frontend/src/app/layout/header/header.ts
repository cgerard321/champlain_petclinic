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
}
