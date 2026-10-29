import { Component, computed, DestroyRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  catchError,
  concatMap,
  forkJoin,
  from,
  map,
  Observable,
  of,
  reduce,
  startWith,
  Subject,
  switchMap,
  toArray,
} from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { VetEditForm, VetSavePayload } from '@features/vets/components/vet-edit-form/vet-edit-form';
import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { VetResponse } from '@features/vets/models/vet-api.model';
import { toVetProfile } from '@features/vets/models/vet-profile.mapper';
import { VetService, vetErrorMessage } from '@features/vets/services/vet.service';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';
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
  private readonly visitService = inject(VisitService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  private readonly editForm = viewChild(VetEditForm);
  protected readonly availableSpecialties: readonly string[] = [
    'radiology',
    'surgery',
    'dentistry',
    'general',
  ];
  protected readonly vet = signal<VetResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly saveError = signal('');
  protected readonly saving = signal(false);
  protected readonly editing = signal(false);
  protected readonly saved = signal(false);
  protected readonly visits = signal<readonly Visit[]>([]);
  protected readonly visitsStatus = signal<'loading' | 'ready' | 'failed'>('loading');
  protected readonly warning = signal('');
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
    this.warning.set('');
    this.editing.set(true);
    this.loadVisits();
  }

  private loadVisits(): void {
    const vet = this.vet();
    if (!vet) return;
    this.visits.set([]);
    this.visitsStatus.set('loading');
    this.visitService
      .getVisitsByPractitionerId(vet.vetId)
      .pipe(
        toArray(),
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((visits) => {
        if (visits) {
          this.visits.set(visits);
          this.visitsStatus.set('ready');
        } else {
          this.visitsStatus.set('failed');
        }
      });
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

  protected save(payload: VetSavePayload): void {
    const vet = this.vet();
    if (!vet || !this.canEdit() || this.saving()) return;
    this.saving.set(true);
    this.saveError.set('');
    this.warning.set('');
    this.service
      .updateVet(vet.vetId, payload.request)
      .pipe(
        concatMap((updatedVet) => {
          const existingNames = new Set(
            (updatedVet.specialties ?? []).map((specialty) => specialty.name.trim().toLowerCase()),
          );

          const namesToAdd = payload.newSpecialtyNames
            .map((name) => name.trim())
            .filter((name) => {
              const key = name.toLowerCase();

              if (!key || existingNames.has(key)) return false;

              existingNames.add(key);
              return true;
            });

          return from(namesToAdd).pipe(
            concatMap((name) => this.service.addSpecialty(vet.vetId, name)),
            reduce((_previous: VetResponse, latest: VetResponse) => latest, updatedVet),
          );
        }),
        switchMap((updated) =>
          this.cancelVisits(payload.visitsToCancel).pipe(map((failed) => ({ updated, failed }))),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ updated, failed }) => {
          this.vet.set(updated);
          this.saving.set(false);
          this.editing.set(false);
          this.saved.set(true);
          this.warning.set(this.cancelWarning(failed));
        },
        error: (error: unknown) => {
          this.saveError.set(vetErrorMessage(error));
          this.saving.set(false);
        },
      });
  }

  private cancelVisits(visits: readonly Visit[]): Observable<number> {
    if (!visits.length) return of(0);
    const details = 'Veterinarian no longer available at this time.';
    return forkJoin(
      visits.map((visit) =>
        this.visitService.cancelVisit(visit.visitId, details).pipe(
          map(() => true),
          catchError(() => of(false)),
        ),
      ),
    ).pipe(map((results) => results.filter((done) => !done).length));
  }

  private cancelWarning(failed: number): string {
    if (failed === 1) {
      return $localize`:@@vets.availability.cancel_failed_one:Le profil est enregistré, mais une visite n'a pas pu être annulée : elle reste active. Signalez-la à un administrateur.`;
    }
    if (failed > 1) {
      return $localize`:@@vets.availability.cancel_failed_many:Le profil est enregistré, mais ${failed}:count: visites n'ont pas pu être annulées : elles restent actives. Signalez-les à un administrateur.`;
    }
    return '';
  }
}
