import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { FormsModule } from '@angular/forms';
import { AuthState } from '@core/services/auth-state';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-sidenav',
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatListModule, FormsModule],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
})
export class Sidenav {
  protected readonly authState: AuthState = inject(AuthState);

  protected employeeName: string = this.authState.userName();

  protected readonly items: NavItem[] = [
    { label: 'Dashboard', route: '/home', icon: 'dashboard' },
    { label: 'Appointments', route: '/appointments', icon: 'event' },
    { label: 'Patients', route: '/patients', icon: 'pets' },
    { label: 'Staff', route: '/staff', icon: 'badge' },
    { label: 'Reports', route: '/reports', icon: 'bar_chart' },
    { label: 'Settings', route: '/settings', icon: 'settings' },
  ];

  protected applyNameChange(): void {
    if (!this.employeeName.trim()) return;
    this.authState.setUserName(this.employeeName);
  }
}
