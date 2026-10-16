import { Routes } from '@angular/router';

import { VetDetail } from '@features/vets/pages/vet-detail/vet-detail';
import { VetList } from '@features/vets/pages/vet-list/vet-list';
import { Veterinarians } from '@features/vets/pages/veterinarians/veterinarians';

export default [
  { path: '', component: Veterinarians },
  { path: 'list', component: VetList },
  {
    path: ':vetId',
    component: VetDetail,
    canDeactivate: [(component: VetDetail) => component.canLeave()],
  },
] satisfies Routes;
