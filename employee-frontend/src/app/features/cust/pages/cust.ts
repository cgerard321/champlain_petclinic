import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-cust',
  styleUrl: './cust.css',
  templateUrl: './cust.html',
})
export class Cust {
  protected auth = inject(AuthState);
}
