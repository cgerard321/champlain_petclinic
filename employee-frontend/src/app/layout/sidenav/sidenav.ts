import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthState } from '@core/services/auth-state';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-sidenav',
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatListModule],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
})
export class Sidenav {
  protected readonly authState = inject(AuthState);

  protected readonly items: NavItem[] = [
    // { label: 'Home', route: '/home', icon: 'home' },
    { label: 'Dummy #1', route: '/dummy1', icon: 'download' },
    { label: 'Dummy #2', route: '/dummy2', icon: 'settings' },
  ];
}
