import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { FilterButton } from '@features/vist/components/FilterButton/filter-button';
import { Status } from '@features/vist/models/Status';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-visit-sidebar',
  imports: [FilterButton, RouterLink],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  private readonly auth = inject(AuthState);

  protected readonly canViewCalendar = computed(
    () => this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.vet),
  );

  readonly hide = output<void>();
  readonly selectedStatuses = input.required<Status[]>();
  readonly statusSelectionChange = output<Status[]>();
}
