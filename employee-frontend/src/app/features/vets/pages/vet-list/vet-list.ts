import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { filter, switchMap } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { VetEducationResponse } from '@features/vets/models/vet-api.model';
import { toVetProfile } from '@features/vets/models/vet-profile.mapper';
import { VetProfile } from '@features/vets/models/vet-profile.model';
import { VetService, vetErrorMessage } from '@features/vets/services/vet.service';
import { ConfirmDialog, ConfirmDialogData } from '@shared/components/confirm-dialog/confirm-dialog';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-vet-list',
  imports: [RouterLink, VetProfileCard],
  templateUrl: './vet-list.html',
  styleUrl: './vet-list.css',
})
export class VetList {
  private readonly service = inject(VetService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly auth = inject(AuthState);
  private readonly dialog = inject(MatDialog);

  protected readonly vets = signal<VetProfile[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly deleteError = signal('');
  protected readonly deleteSuccess = signal('');
  protected readonly deletingVetId = signal<string | null>(null);
  protected readonly searchQuery = signal('');
  protected readonly selectedSpecialty = signal('');
  protected readonly educationsByVet = signal<Partial<Record<string, VetEducationResponse[]>>>({});

  protected readonly isAdmin = computed(() => this.auth.hasRole(Roles.admin));

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
    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set('');

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
        next: (vets) => {
          const activeVets = vets.filter((vet) => vet.active === true).map(toVetProfile);

          this.vets.set(activeVets);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          console.error('[VETS] Failed to load:', error);
          this.error.set(vetErrorMessage(error));
          this.loading.set(false);
        },
      });
  }

  protected removeVet(vet: VetProfile): void {
    if (!this.auth.hasRole(Roles.admin) || this.deletingVetId()) {
      return;
    }

    this.deleteError.set('');
    this.deleteSuccess.set('');

    const name = `${vet.firstName} ${vet.lastName}`;

    const dialogData: ConfirmDialogData = {
      title: $localize`:@@vets.remove.title:Supprimer un vétérinaire`,
      message: $localize`:@@vets.remove.confirm:Voulez-vous vraiment supprimer ${name}:name: ?`,
      confirmLabel: $localize`:@@vets.remove.confirm_button:Supprimer`,
      cancelLabel: $localize`:@@vets.remove.cancel:Annuler`,
    };

    this.dialog
      .open(ConfirmDialog, {
        width: '420px',
        data: dialogData,
      })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        filter(() => this.auth.hasRole(Roles.admin) && !this.deletingVetId()),
        switchMap(() => {
          this.deletingVetId.set(vet.vetId);
          return this.service.deleteVet(vet.vetId);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.vets.update((current) => current.filter((item) => item.vetId !== vet.vetId));

          this.deleteSuccess.set(
            $localize`:@@vets.remove.success:Le vétérinaire a été supprimé avec succès.`,
          );

          this.deletingVetId.set(null);
        },
        error: (error: unknown) => {
          this.deleteError.set(vetErrorMessage(error));
          this.deletingVetId.set(null);
        },
      });
  }
}
