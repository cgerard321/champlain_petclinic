import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { VetCreateForm } from '@features/vets/components/vet-create-form/vet-create-form';
import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { toVetProfile } from '@features/vets/models/vet-profile.mapper';
import { VetProfile } from '@features/vets/models/vet-profile.model';
import { VetService, vetErrorMessage } from '@features/vets/services/vet.service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-vet-list',
  imports: [RouterLink, VetProfileCard, VetCreateForm],
  templateUrl: './vet-list.html',
  styleUrl: './vet-list.css',
})
export class VetList {
  private readonly auth = inject(AuthState);
  protected readonly canCreate = computed(() => this.auth.hasRole(Roles.admin));
  protected readonly adding = signal(
    inject(ActivatedRoute).snapshot.queryParamMap.get('add') === '1',
  );
  protected readonly added = signal(false);

  protected created(): void {
    this.adding.set(false);
    this.added.set(true);
    this.load();
  }

  private listRequest?: Subscription;
  private readonly service = inject(VetService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly vets = signal<VetProfile[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly searchQuery = signal('');
  protected readonly selectedSpecialty = signal('');

  protected readonly specialties = computed(() => {
    return Array.from(new Set(this.vets().flatMap((vet) => vet.specialties))).sort();
  });

  protected readonly filteredVets = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const specialty = this.selectedSpecialty();
    return this.vets().filter((vet) => {
      const matchesName = `${vet.firstName} ${vet.lastName}`.toLowerCase().includes(query);
      const matchesSpecialty = !specialty || vet.specialties.includes(specialty);
      return matchesName && matchesSpecialty;
    });
  });

  protected readonly hasActiveFilters = computed(
    () => this.searchQuery() !== '' || this.selectedSpecialty() !== '',
  );

  constructor() {
    this.load();
  }

  protected onSearch(event: Event): void {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  protected onSpecialtyChange(event: Event): void {
    this.selectedSpecialty.set((event.target as HTMLSelectElement).value);
  }

  protected clearFilters(): void {
    this.searchQuery.set('');
    this.selectedSpecialty.set('');
  }

  protected load(): void {
    this.listRequest?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.listRequest = this.service
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
