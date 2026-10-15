import { Routes } from '@angular/router';

import { VisitDetails } from '@features/vist/pages/visit-details/visit-details';
import { Vist } from '@features/vist/pages/vist';

export default [
  { path: '', pathMatch: 'full', component: Vist },
  { path: ':visitId', component: VisitDetails },
] satisfies Routes;
