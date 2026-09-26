import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-vets',
  styleUrl: './vets.css',
  templateUrl: './vets.html',
})
export class Vets {
  protected auth = inject(AuthState);
}
