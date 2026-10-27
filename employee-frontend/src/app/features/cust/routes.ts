import { Routes } from '@angular/router';

import { Cust } from '@features/cust/pages/cust';
import { PetList } from '@features/cust/pages/pet-list/pet-list';

export default [
  { path: '', component: Cust },
  { path: 'pets', component: PetList },
] satisfies Routes;
