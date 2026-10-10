import { Component, inject } from '@angular/core';

import { AuthState } from '@core/services/auth-state';
import { VistListTable } from '@features/vist/components/TableVisits/VistListTable';
// import { ComingSoon } from '@shared/components/coming-soon/coming-soon';

@Component({
  imports: [VistListTable],
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
}
