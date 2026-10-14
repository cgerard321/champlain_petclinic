import { Routes } from '@angular/router';

import { VetList } from '@features/vets/pages/vet-list/vet-list';
import { Veterinarians } from '@features/vets/pages/veterinarians/veterinarians';

export default [
  { path: '', component: Veterinarians },
  { path: 'list', component: VetList },
] satisfies Routes;
