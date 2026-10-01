import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';
import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [ComingSoon],
  selector: 'app-settings',
  styleUrl: './settings.css',
  templateUrl: './settings.html',
})
export class Settings {
  protected auth = inject(AuthState);
}
