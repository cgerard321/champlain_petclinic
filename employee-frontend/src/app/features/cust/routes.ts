import { Routes } from '@angular/router';
import { PetList } from '@features/cust/pages/pet-list/pet-list';

import { Cust } from '@features/cust/pages/cust';

export default [
  { path: '', component: Cust },
  { path: 'pets', component: PetList },
] satisfies Routes;
