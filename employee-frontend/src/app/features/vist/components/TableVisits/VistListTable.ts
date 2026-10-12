import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthState } from '@core/services/auth-state';
import { FollowUpVisitDialog } from '@features/vist/components/FollowUpVisit/FollowUpVisitDialog';
import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-visit',
  imports: [DatePipe, MatButtonModule, MatDialogModule, MatSnackBarModule],
  styleUrl: './VistListTable.css',
  templateUrl: './VistListTable.html',
})
export class VistListTable implements OnInit {
  private readonly visitService = inject(VisitService);
  private readonly authState = inject(AuthState);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly untilDestroyed = takeUntilDestroyed<Visit>();
  protected readonly visit = signal<Visit[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly canScheduleFollowUp = this.authState.hasRole(Roles.vet);

  protected statusLabel(status: Status): string {
    switch (status) {
      case Status.SCHEDULED:
        return $localize`:@@visits.statusScheduled:Planifiée`;
      case Status.COMPLETED:
        return $localize`:@@visits.statusCompleted:Complétée`;
      case Status.CANCELLED:
        return $localize`:@@visits.statusCancelled:Annulée`;
      case Status.CONFIRMED:
        return $localize`:@@visits.statusConfirmed:Confirmée`;
      case Status.UPCOMING:
        return $localize`:@@visits.statusUpcoming:À venir`;
      case Status.ARCHIVED:
        return $localize`:@@visits.statusArchived:Archivée`;
      case Status.OUT_OF_STATUS:
        return $localize`:@@visits.statusOutOfStatus:Hors statut`;
    }
  }

  ngOnInit(): void {
    this.visitService
      .getAllVisits()
      .pipe(this.untilDestroyed)
      .subscribe({
        next: (visits) =>
          this.visit.update((visit) =>
            [...visit, visits].sort(
              (a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime(),
            ),
          ),
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set(
            $localize`:@@visitListLoadError:Unable to load visits. Please try again later`,
          );
        },
        complete: () => this.isLoading.set(false),
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