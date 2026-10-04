import { Routes } from '@angular/router';

import { Bill } from '@features/bill/pages/bill';
import { CreateBill } from '@features/bill/pages/createBill/createBill';


export default [
  { path: '', component: Bill },
  { path: 'create', component: CreateBill },
] satisfies Routes;
