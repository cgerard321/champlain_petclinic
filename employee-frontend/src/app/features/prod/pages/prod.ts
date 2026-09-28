import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [ComingSoon],
  selector: 'app-prod',
  styleUrl: './prod.css',
  templateUrl: './prod.html',
})
export class Prod {
  protected auth = inject(AuthState);
}
