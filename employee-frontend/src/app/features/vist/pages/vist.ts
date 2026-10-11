import { Component, inject, signal } from '@angular/core';

import { AuthState } from '@core/services/auth-state';
import { Sidebar } from '@features/vist/components/Sidebar/sidebar';
import { VistListTable } from '@features/vist/components/TableVisits/VistListTable';
// import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [VistListTable, Sidebar],
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

  protected hideSidebar(): void {
    this.isSidebarVisible.set(false);
  }

  protected showSidebar(): void {
    this.isSidebarVisible.set(true);
  }
}
