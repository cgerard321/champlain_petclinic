import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [ComingSoon],
  selector: 'app-vist',
  styleUrl: './vist.css',
  templateUrl: './vist.html',
})
export class Vist {
  protected auth = inject(AuthState);
}
