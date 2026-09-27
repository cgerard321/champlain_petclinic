import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-vist',
  styleUrl: './vist.css',
  templateUrl: './vist.html',
})
export class Vist {
  protected auth = inject(AuthState);
}
