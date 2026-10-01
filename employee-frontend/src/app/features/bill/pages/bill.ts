import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';
import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [ComingSoon],
  selector: 'app-bill',
  styleUrl: './bill.css',
  templateUrl: './bill.html',
})
export class Bill {
  protected auth = inject(AuthState);
}
