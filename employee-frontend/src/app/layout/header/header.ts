import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthState } from '@core/services/auth-state';

@Component({
  selector: 'app-header',
  imports: [MatIconModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  protected readonly authState: any = inject(AuthState);
  private router = inject(Router);

  get currentName(): string {
    if (typeof this.authState.user === 'function') {
      return this.authState.user()?.name || 'Sarah Jenkins';
    }
    return this.authState.user?.name || 'Sarah Jenkins';
  }

  get initials(): string {
    const name = this.currentName.trim();
    if (!name) return 'SJ';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  protected logout(): void {
    if (typeof this.authState.logout === 'function') {
      this.authState.logout();
    } else if (typeof this.authState.clear === 'function') {
      this.authState.clear();
    }
    this.router.navigate(['/login']);
  }
}
