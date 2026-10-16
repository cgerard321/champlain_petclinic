import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { timer } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { FollowUpVisitDialog } from '@features/vist/components/FollowUpVisit/FollowUpVisitDialog';
import { Status } from '@features/vist/models/Status';
import { visitStatusLabel } from '@features/vist/models/status-label';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-next-appointment',
  imports: [DatePipe, MatButtonModule, MatDialogModule, MatSnackBarModule],
  styleUrl: './NextAppointment.css',
  templateUrl: './NextAppointment.html',
})
export class NextAppointment implements OnInit {
  private readonly visitService = inject(VisitService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authState = inject(AuthState);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private notificationShown = false;
  private readonly untilDestroyed = takeUntilDestroyed<Visit>();
  protected readonly visit = signal<Visit[]>([]);
  private readonly currentTime = signal(Date.now());
  protected readonly statusLabel = visitStatusLabel;
  readonly selectedStatuses = input<Status[]>(Object.values(Status));
  protected readonly closestUpcomingVisit = computed(() => {
    const now = this.currentTime();

    return (
      this.visit()
        .filter((visit) => new Date(visit.visitDate).getTime() >= now)
        .sort((a, b) => new Date(a.visitDate).getTime() - new Date(b.visitDate).getTime())[0] ??
      null
    );
  });

  protected readonly visibleVisits = computed(() => {
    const appointment = this.closestUpcomingVisit();
    return appointment ? [appointment] : [];
  });

  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly canScheduleFollowUp = this.authState.hasRole(Roles.vet);

  ngOnInit(): void {
    timer(0, 60_000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.currentTime.set(Date.now()));

    this.visitService
      .getAllVisits()
      .pipe(this.untilDestroyed)
      .subscribe({
        next: (visits) => {
          this.visit.update((currentVisits) =>
            [...currentVisits, visits].sort(
              (a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime(),
            ),
          );
        },
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set(
            $localize`:@@visitListLoadError:Unable to load visits. Please try again later`,
          );
        },
        complete: () => {
          this.isLoading.set(false);
          this.notifyUpcomingAppointment();
        },
      });
  }

  private notifyUpcomingAppointment(): void {
    const now = Date.now();
    const sevenDaysFromNow = now + 7 * 24 * 60 * 60 * 1000;

    if (this.notificationShown) {
      return;
    }

    const upcomingVisit = this.visit()
      .filter((visit) => {
        const visitTime = new Date(visit.visitDate).getTime();

        return (
          visitTime >= now &&
          visitTime <= sevenDaysFromNow &&
          [Status.UPCOMING, Status.SCHEDULED, Status.CONFIRMED].includes(visit.status)
        );
      })
      .sort((a, b) => new Date(a.visitDate).getTime() - new Date(b.visitDate).getTime())[0];

    if (!upcomingVisit) {
      return;
    }

    const lang = localStorage.getItem('lang') ?? 'fr';
    const isEnglish = lang.startsWith('en');

    const appointmentDate = new Date(upcomingVisit.visitDate).toLocaleString(
      isEnglish ? 'en-CA' : 'fr-CA',
    );

    const message = isEnglish
      ? `Your next appointment for ${upcomingVisit.petName} is on ${appointmentDate}.`
      : `Votre prochain rendez-vous pour ${upcomingVisit.petName} est le ${appointmentDate}.`;

    this.notificationShown = true;

    this.snackBar.open(message, '', {
      duration: 6000,
      verticalPosition: 'top',
    });
  }

  protected openFollowUpVisit(visit: Visit): void {
    this.dialog
      .open(FollowUpVisitDialog, {
        data: { visit },
        width: '620px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .subscribe((followUpVisit?: Visit) => {
        if (followUpVisit) {
          this.visit.update((visits) =>
            [...visits, followUpVisit].sort(
              (a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime(),
            ),
          );
          this.snackBar.open(
            $localize`:@@visits.followUp.confirmation:La visite de suivi a été planifiée.`,
            '',
            { duration: 3000, verticalPosition: 'top' },
          );
        }
      });
  }
}
