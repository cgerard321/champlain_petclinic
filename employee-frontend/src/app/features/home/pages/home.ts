import { Component, inject } from '@angular/core';
import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
})
export class Home {
  protected auth = inject(AuthState);
}
