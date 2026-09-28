import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-prod',
  styleUrl: './prod.css',
  templateUrl: './prod.html',
})
export class Prod {
  protected auth = inject(AuthState);
}
