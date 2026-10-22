import { Component, computed, DestroyRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of, startWith, Subject, switchMap } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { VetEditForm } from '@features/vets/components/vet-edit-form/vet-edit-form';
import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { VetResponse, VetUpdateRequest } from '@features/vets/models/vet-api.model';
import { toVetProfile } from '@features/vets/models/vet-profile.mapper';
import { VetService, vetErrorMessage } from '@features/vets/services/vet.service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-vet-detail',
  imports: [RouterLink, VetProfileCard, VetEditForm],
  templateUrl: './vet-detail.html',
  styleUrl: './vet-detail.css',
})
export class VetDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(VetService);
  private readonly auth = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  private readonly editForm = viewChild(VetEditForm);
  protected readonly vet = signal<VetResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly saveError = signal('');
  protected readonly saving = signal(false);
  protected readonly editing = signal(false);
  protected readonly saved = signal(false);
  protected readonly profile = computed(() => {
    const vet = this.vet();
    return vet ? toVetProfile(vet) : null;
  });
  protected readonly canEdit = computed(
    () =>
      this.auth.hasRole(Roles.admin) ||
      (this.auth.hasRole(Roles.vet) &&
        !!this.auth.userId() &&
        this.auth.userId() === this.vet()?.vetId),
  );

  constructor() {
    this.route.paramMap
      .pipe(
        switchMap((params) =>
          this.reload.pipe(
            startWith(undefined),
            map(() => params.get('vetId') ?? ''),
          ),
        ),
        switchMap((id) => {
          this.loading.set(true);
          this.error.set('');
          this.vet.set(null);
          this.editing.set(false);
          this.saved.set(false);
          this.saveError.set('');
          if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)) {
            return of({
              vet: null,
              error: $localize`:@@vets.errors.not_found:Ce profil vétérinaire est introuvable.`,
            });
          }
          return this.service.getVetById(id).pipe(
            map((vet) => ({ vet, error: '' })),
            catchError((error: unknown) => of({ vet: null, error: vetErrorMessage(error) })),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        this.vet.set(result.vet);
        this.error.set(result.error);
        this.loading.set(false);
      });
  }

  protected retry(): void {
    this.reload.next();
  }

  protected edit(): void {
    if (!this.canEdit()) return;
    this.saved.set(false);
    this.saveError.set('');
    this.editing.set(true);
  }

  protected cancel(): void {
    if (this.saving()) return;
    this.editing.set(false);
    this.saveError.set('');
  }

  canLeave(): boolean {
    if (this.saving()) return false;
    return (
      !this.editForm()?.hasUnsavedChanges() ||
      window.confirm(
        $localize`:@@vets.edit.leave:Quitter cette page et abandonner les modifications non enregistrées ?`,
      )
    );
  }

  protected save(request: VetUpdateRequest): void {
    const vet = this.vet();
    if (!vet || !this.canEdit() || this.saving()) return;
    this.saving.set(true);
    this.saveError.set('');
    this.service
      .updateVet(vet.vetId, request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.vet.set(updated);
          this.saving.set(false);
          this.editing.set(false);
          this.saved.set(true);
        },
        error: (error: unknown) => {
          this.saveError.set(vetErrorMessage(error));
          this.saving.set(false);
        },
      });
  }
}
