import { Component, computed, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-veterinarians',
  imports: [MatIconModule, RouterLink],
  templateUrl: './veterinarians.html',
  styleUrl: './veterinarians.css',
})
export class Veterinarians {
  private readonly auth = inject(AuthState);
  protected readonly canCreate = computed(() => this.auth.hasRole(Roles.admin));
}
