import { Component, computed, input } from '@angular/core';

import { specialtyLabel } from '@features/vets/models/vet-profile.mapper';
import { VetProfile } from '@features/vets/models/vet-profile.model';

@Component({
  selector: 'app-vet-profile-card',
  templateUrl: './vet-profile-card.html',
  styleUrl: './vet-profile-card.css',
})
export class VetProfileCard {
  readonly vet = input.required<VetProfile>();
  protected readonly specialtyLabel = specialtyLabel;
  protected readonly initials = computed(() => {
    const { firstName, lastName } = this.vet();
    return `${firstName.trim().charAt(0)}${lastName.trim().charAt(0)}`.toLocaleUpperCase();
  });
}
