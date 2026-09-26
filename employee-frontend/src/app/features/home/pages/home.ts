import { Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

import { AuthState } from '@core/services/auth-state';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatCardModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  protected readonly authState = inject(AuthState);
}
