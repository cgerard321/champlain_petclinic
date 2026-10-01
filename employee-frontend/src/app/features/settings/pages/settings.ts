import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [RouterLink, MatButtonModule, MatIconModule, ComingSoon],
  selector: 'app-settings',
  styleUrl: './settings.css',
  templateUrl: './settings.html',
})
export class Settings {
  protected auth = inject(AuthState);
}
