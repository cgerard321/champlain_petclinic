import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatListModule],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
})
export class Sidenav {
  protected readonly items: NavItem[] = [
    { label: 'Dashboard', route: '/home', icon: 'dashboard' },
    { label: 'Appointments', route: '/appointments', icon: 'event' },
    { label: 'Patients', route: '/patients', icon: 'pets' },
    { label: 'Staff', route: '/staff', icon: 'badge' },
    { label: 'Reports', route: '/reports', icon: 'bar_chart' },
    { label: 'Settings', route: '/settings', icon: 'settings' },
  ];
}