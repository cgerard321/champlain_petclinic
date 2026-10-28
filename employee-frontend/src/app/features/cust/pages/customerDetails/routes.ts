import { Routes } from '@angular/router';

import { authGuard } from '@core/guards/auth/auth-guard';
import { CustomerDetails } from '@features/cust/pages/customerDetails/customerDetails';
import { Roles } from '@shared/models/roles';

export default [
  {
    path: ':customerId',
    component: CustomerDetails,
    canActivate: [authGuard([Roles.admin, Roles.receptionist])],
  },
] satisfies Routes;
