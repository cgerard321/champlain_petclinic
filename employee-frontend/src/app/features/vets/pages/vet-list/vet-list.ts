import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { VET_PROFILE_PREVIEWS } from '@features/vets/pages/vet-list/vet-list.preview-data';

@Component({
  selector: 'app-vet-list',
  imports: [RouterLink, VetProfileCard],
  templateUrl: './vet-list.html',
  styleUrl: './vet-list.css',
})
export class VetList {
  protected readonly vets = VET_PROFILE_PREVIEWS;
}
