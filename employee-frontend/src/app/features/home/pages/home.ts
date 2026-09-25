import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthState } from '@core/services/auth-state';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  protected readonly authState = inject(AuthState);

  get currentName(): string {
    return this.authState.userName();
  }
}
