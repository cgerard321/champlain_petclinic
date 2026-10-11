import { Component, output } from '@angular/core';

import { FilterButton } from '@features/vist/components/FilterButton/filter-button';

@Component({
  selector: 'app-visit-sidebar',
  imports: [FilterButton],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  protected readonly hide = output<void>();
}
