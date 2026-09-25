import { Component, inject } from '@angular/core';
import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-bill',
  styleUrl: './bill.css',
  templateUrl: './bill.html',
})
export class Bill {
  protected auth = inject(AuthState);
}
