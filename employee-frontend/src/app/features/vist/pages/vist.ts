import { Component, inject, signal } from '@angular/core';

import { AuthState } from '@core/services/auth-state';
import { NextAppointment } from '@features/vist/components/NextAppointment/NextAppointment';
import { Sidebar } from '@features/vist/components/Sidebar/sidebar';
import { VistListTable } from '@features/vist/components/TableVisits/VistListTable';
import { Status } from '@features/vist/models/Status';
// import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [VistListTable, Sidebar, NextAppointment],
  selector: 'app-vist',
  styleUrl: './vist.css',
  templateUrl: './vist.html',
})
// @Component({
//   selector: 'app-visit-admin',
//   imports: [VistListTableAdmin],
//   styleUrl: './VistListTableAdmin.css',
//   templateUrl: './VistListTableAdmin.html',
// })
export class Vist {
  protected auth = inject(AuthState);
  protected readonly isSidebarVisible = signal(true);
  protected readonly selectedStatuses = signal<Status[]>(Object.values(Status));

  protected hideSidebar(): void {
    this.isSidebarVisible.set(false);
  }

  protected showSidebar(): void {
    this.isSidebarVisible.set(true);
  }

  protected updateSelectedStatuses(statuses: Status[]): void {
    this.selectedStatuses.set(statuses);
  }
}
