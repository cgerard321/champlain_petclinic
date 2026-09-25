import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { AuthState } from '@core/services/auth-state';
import { EMPLOYEE_ROLES } from '@shared/models/roles';

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

  // While I was here, I made "items" into a signal in accordance with Angular best practices
  protected readonly items = computed<NavItem[]>(() => {
    const items = [
      { label: 'Home', route: '/home', icon: 'home' },
      { label: 'Dummy #1', route: '/dummy1', icon: 'download' },
      { label: 'Dummy #2', route: '/dummy2', icon: 'settings' },
    ];

    // Only shows the inventory option if the user is an employee
    if (EMPLOYEE_ROLES.some((role) => this.authState.hasRole(role))) {
      items.push({ label: 'Inventory', route: '/inventory', icon: 'inventory_2' });
    }

    return items;
  });
}
