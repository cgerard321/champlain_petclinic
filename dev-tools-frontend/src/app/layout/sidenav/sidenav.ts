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
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatListModule],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
})
export class Sidenav {
  protected readonly items: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
    { label: 'Log reading', route: '/logs', icon: 'terminal' }
  ];
}
