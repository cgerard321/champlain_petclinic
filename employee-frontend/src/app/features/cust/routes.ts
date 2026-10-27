import { Routes } from '@angular/router';

import { Cust } from '@features/cust/pages/cust';
import { PetTypeList } from '@features/cust/pages/pet-type-list/pet-type-list';


export default [{ path: '', component: Cust },
  { path: 'pet-types', component: PetTypeList },
] satisfies Routes;
