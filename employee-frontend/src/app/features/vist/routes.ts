import { Routes } from '@angular/router';

import { Vist } from '@features/vist/pages/vist';
import { VisitDetails } from '@features/vist/pages/visit-details/visit-details';

export default [{ path: '', pathMatch: 'full', component: Vist }, { path: ':visitId', component: VisitDetails }] satisfies Routes;
