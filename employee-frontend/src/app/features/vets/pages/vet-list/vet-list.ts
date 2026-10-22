import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { toVetProfile } from '@features/vets/models/vet-profile.mapper';
import { VetProfile } from '@features/vets/models/vet-profile.model';
import { VetService, vetErrorMessage } from '@features/vets/services/vet.service';

@Component({
  selector: 'app-vet-list',
  imports: [RouterLink, VetProfileCard],
  templateUrl: './vet-list.html',
  styleUrl: './vet-list.css',
})
export class VetList {
  private readonly service = inject(VetService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly vets = signal<VetProfile[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  constructor() {
    this.load();
  }

  protected load(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set('');
    this.service
      .getVets()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (vets) => {
          this.vets.set(vets.map(toVetProfile));
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.error.set(vetErrorMessage(error));
          this.loading.set(false);
        },
      });
  }
}
