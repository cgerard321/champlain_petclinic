import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router } from '@angular/router';

import { AuthState } from '@core/services/auth-state';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatIconModule, MatToolbarModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  protected readonly authState = inject(AuthState);
  private readonly router = inject(Router);

  protected logout(): void {
    this.authState.logout();
    this.router.navigate(['/login']);
  }
}
