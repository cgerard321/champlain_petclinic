import { Component, input, output } from '@angular/core';

import { FilterButton } from '@features/vist/components/FilterButton/filter-button';
import { Status } from '@features/vist/models/Status';

@Component({
  selector: 'app-visit-sidebar',
  imports: [FilterButton],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  readonly hide = output<void>();
  readonly selectedStatuses = input.required<Status[]>();
  readonly statusSelectionChange = output<Status[]>();
}
