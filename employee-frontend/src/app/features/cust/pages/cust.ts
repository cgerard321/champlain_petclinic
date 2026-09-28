import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [ComingSoon],
  selector: 'app-cust',
  styleUrl: './cust.css',
  templateUrl: './cust.html',
})
export class Cust {
  protected auth = inject(AuthState);
}
