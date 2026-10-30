import { Component, DestroyRef, inject, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';

import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { VetEducationResponse } from '@features/vets/models/vet-api.model';
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
  protected readonly searchQuery = signal('');
  protected readonly selectedSpecialty = signal('');
  protected readonly educationsByVet = signal<Partial<Record<string, VetEducationResponse[]>>>({});

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
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set('');
    this.educationsByVet.set({});
    this.service
      .getVets()
      .pipe(
        switchMap((vets) => {
          const profiles = vets.map(toVetProfile);

          if (!profiles.length) {
            return of({
              profiles,
              educationsByVetId: {},
            });
          }
          return forkJoin(
            profiles.map((vet) =>
              this.service.getEducations(vet.vetId).pipe(
                map((educations) => ({
                  vetId: vet.vetId,
                  educations,
                })),
              ),
            ),
          ).pipe(
            map((results) => {
              const educationsByVetId: Partial<Record<string, VetEducationResponse[]>> = {};
              for (const result of results) {
                educationsByVetId[result.vetId] = result.educations;
              }

              return {
                profiles,
                educationsByVetId,
              };
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ profiles, educationsByVetId }) => {
          this.vets.set(profiles);
          this.educationsByVet.set(educationsByVetId);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.error.set(vetErrorMessage(error));
          this.loading.set(false);
        },
      });
  }
}
