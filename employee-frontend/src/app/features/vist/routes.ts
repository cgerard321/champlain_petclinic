import { Routes } from '@angular/router';

import { VisitDetails } from '@features/vist/pages/visit-details/visit-details';
import { Vist } from '@features/vist/pages/vist';
import {WeeklyCalendar} from '@features/vist/pages/weekly-calendar/weekly-calendar';
import {authGuard} from '@core/guards/auth/auth-guard';
import {Roles} from '@shared/models/roles';

export default [
  { path: '', pathMatch: 'full', component: Vist },
  {
    path: 'calendar',
    component: WeeklyCalendar,
    canActivate: [authGuard([Roles.admin, Roles.vet])],
  },
  { path: ':visitId', component: VisitDetails },
] satisfies Routes;
