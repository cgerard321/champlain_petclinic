import { Component, computed, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-bill',
  styleUrl: './bill.css',
  templateUrl: './bill.html',
})
export class Bill {
  protected readonly auth = inject(AuthState);

  // Only vets can start a bill. The CRUD work is handled in the follow-up stories.
  protected readonly canCreateBill = computed(() => this.auth.hasRole(Roles.vet));
}
