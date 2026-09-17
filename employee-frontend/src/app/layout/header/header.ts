import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';

import { AuthState } from '@core/services/auth-state';

@Component({
  selector: 'app-header',
  imports: [MatButtonModule, MatIconModule, MatToolbarModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private readonly authState = inject(AuthState);
  private readonly router = inject(Router);

  protected logout(): void {
    this.authState.logout().subscribe(() => this.router.navigateByUrl('/login'));
  }
}
