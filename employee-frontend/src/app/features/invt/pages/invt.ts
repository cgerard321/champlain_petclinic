import { Component, inject } from '@angular/core';
import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-invy',
  styleUrl: './invt.css',
  templateUrl: './invt.html',
})
export class Invt {
  protected auth = inject(AuthState);
}
