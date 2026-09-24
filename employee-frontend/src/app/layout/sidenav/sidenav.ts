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
  protected readonly authState: any = inject(AuthState);

  protected employeeName: string = this.getInitialName();

  protected readonly items: NavItem[] = [
    { label: 'Dashboard', route: '/home', icon: 'dashboard' },
    { label: 'Appointments', route: '/appointments', icon: 'event' },
    { label: 'Patients', route: '/patients', icon: 'pets' },
    { label: 'Staff', route: '/staff', icon: 'badge' },
    { label: 'Reports', route: '/reports', icon: 'bar_chart' },
    { label: 'Settings', route: '/settings', icon: 'settings' },
  ];

  private getInitialName(): string {
    if (typeof this.authState.user === 'function') {
      return this.authState.user()?.name || 'Sarah Jenkins';
    }
    return this.authState.user?.name || 'Sarah Jenkins';
  }

  protected applyNameChange(): void {
    if (!this.employeeName.trim()) return;

    if (typeof this.authState.setUserName === 'function') {
      this.authState.setUserName(this.employeeName);
    } else if (typeof this.authState.setUser === 'function') {
      const currentUser = typeof this.authState.user === 'function' ? this.authState.user() : this.authState.user;
      this.authState.setUser({ ...currentUser, name: this.employeeName });
    } else if (typeof this.authState.user === 'function' && typeof this.authState.user.set === 'function') {
      this.authState.user.set({ ...this.authState.user(), name: this.employeeName });
    } else if (this.authState.user) {
      this.authState.user.name = this.employeeName;
    }
  }
}
