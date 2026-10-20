import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, effect, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, finalize, map, Observable, of, shareReplay, switchMap } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { Customer } from '@features/cust/models/customer.model';
import { visitStatusLabel } from '@features/vist/models/status-label';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';
import { Roles } from '@shared/models/roles';

interface PetDetails {
  petName: string;
  ownerName: string;
}

function startOfWeek(date: Date): Date {
  const start = new Date(date);
  start.setHours(0, 0, 0, 0);
  start.setDate(start.getDate() - start.getDay());
  return start;
}

@Component({
  selector: 'app-weekly-calendar',
  imports: [DatePipe, RouterLink],
  templateUrl: './weekly-calendar.html',
  styleUrl: './weekly-calendar.css',
})
export class WeeklyCalendar implements OnInit {
  private readonly auth = inject(AuthState);
  private readonly visitService = inject(VisitService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly visits = signal(new Map<string, Visit>());
  private readonly requestedPets = new Set<string>();
  private readonly customerRequests = new Map<string, Observable<Customer | null>>();

  protected readonly statusLabel = visitStatusLabel;
  protected readonly weekStart = signal(startOfWeek(new Date()));
  protected readonly isLoading = signal(true);
  protected readonly hasError = signal(false);
  protected readonly petDetails = signal(new Map<string, PetDetails>());
  protected readonly isSendingEmail = signal(false);
  protected readonly emailMessage = signal('');

  protected readonly labels = {
    title: $localize`:@@visits.calendar.title:Calendrier hebdomadaire`,
    back: $localize`:@@visits.calendar.back:Retour à la liste des visites`,
    previous: $localize`:@@visits.calendar.previous:Semaine précédente`,
    current: $localize`:@@visits.calendar.current:Semaine actuelle`,
    next: $localize`:@@visits.calendar.next:Semaine suivante`,
    empty: $localize`:@@visits.calendar.empty:Aucun rendez-vous trouvé pour ce jour.`,
    loading: $localize`:@@visits.calendar.loading:Chargement des visites...`,
    error: $localize`:@@visits.calendar.error:Impossible de charger les visites.`,
    start: $localize`:@@visits.details.start:Début`,
    end: $localize`:@@visits.details.end:Fin`,
    owner: $localize`:@@visits.calendar.owner:Propriétaire`,
    pet: $localize`:@@visits.petName:Nom de l'animal`,
    type: $localize`:@@visits.calendar.type:Type de rendez-vous`,
    status: $localize`:@@visits.status:Statut`,
    notAvailable: $localize`:@@visits.details.notAvailable:Non disponible`,
  };

  private readonly weekVisits = computed(() => {
    const start = this.weekStart();
    const end = new Date(start);
    end.setDate(end.getDate() + 7);

    return [...this.visits().values()]
      .filter((visit) => {
        const date = new Date(visit.visitDate);
        return date >= start && date < end;
      })
      .sort((a, b) => new Date(a.visitDate).getTime() - new Date(b.visitDate).getTime());
  });

  protected readonly days = computed(() =>
    Array.from({ length: 7 }, (_, index) => {
      const date = new Date(this.weekStart());
      date.setDate(date.getDate() + index);

      return {
        date,
        visits: this.weekVisits().filter(
          (visit) => new Date(visit.visitDate).toDateString() === date.toDateString(),
        ),
      };
    }),
  );

  constructor() {
    effect(() => {
      for (const visit of this.weekVisits()) {
        this.loadPetDetails(visit.petId);
      }
    });
  }

  ngOnInit(): void {
    this.visitService
      .getAllVisits()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (visit) => {
          if (this.auth.hasRole(Roles.admin) || visit.practitionerId === this.auth.userId()) {
            this.visits.update((visits) => new Map(visits).set(visit.visitId, visit));
          }
        },
        error: () => {
          this.hasError.set(true);
          this.isLoading.set(false);
        },
        complete: () => this.isLoading.set(false),
      });
  }

  protected changeWeek(offset: number): void {
    const start = new Date(this.weekStart());
    start.setDate(start.getDate() + offset * 7);
    this.weekStart.set(start);
  }

  protected currentWeek(): void {
    this.weekStart.set(startOfWeek(new Date()));
  }

  private loadPetDetails(petId: string): void {
    if (!petId || this.requestedPets.has(petId)) {
      return;
    }

    this.requestedPets.add(petId);

    this.visitService
      .getPetById(petId)
      .pipe(
        switchMap((pet) =>
          this.getCustomer(pet.customerId).pipe(
            map((customer) => ({
              petName: pet.name,
              ownerName: customer ? `${customer.firstName} ${customer.lastName}`.trim() : '',
            })),
          ),
        ),
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((details) => {
        if (details) {
          this.petDetails.update((pets) => new Map(pets).set(petId, details));
        }
      });
  }

  private getCustomer(customerId: string): Observable<Customer | null> {
    if (!customerId) {
      return of(null);
    }

    let request = this.customerRequests.get(customerId);

    if (!request) {
      request = this.visitService.getCustomerById(customerId).pipe(
        catchError(() => of(null)),
        shareReplay({ bufferSize: 1, refCount: true }),
      );

      this.customerRequests.set(customerId, request);
    }

    return request;
  }

  protected sendWeeklyScheduleEmail(): void {
    this.isSendingEmail.set(true);
    this.emailMessage.set('');

    this.visitService
      .sendWeeklyScheduleEmail()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSendingEmail.set(false)),
      )
      .subscribe({
        next: (message) => this.emailMessage.set(message),
        error: () => this.emailMessage.set('Could not send the schedule email.'),
      });
  }
}
