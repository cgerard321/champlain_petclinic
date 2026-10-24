import { Routes } from '@angular/router';

import { CustomerDetails } from '@features/cust/pages/customerDetails/customerDetails';

export default [{ path: ':customerId', component: CustomerDetails }] satisfies Routes;
