import { Routes } from '@angular/router';

import { CreateBill } from '@features/bill/components/createBill/create-bill';
import { Bill } from '@features/bill/pages/bill';

export default [
  { path: '', component: Bill },
  { path: 'create', component: CreateBill },
] satisfies Routes;
