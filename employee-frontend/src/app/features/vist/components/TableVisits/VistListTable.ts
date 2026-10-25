import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  EMPTY,
  finalize,
  startWith,
  Subject,
  switchMap,
} from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { FollowUpVisitDialog } from '@features/vist/components/FollowUpVisit/FollowUpVisitDialog';
import { VetNoteDialog } from '@features/vist/components/VetNote/VetNoteDialog';
import { Status } from '@features/vist/models/Status';
import { visitStatusLabel } from '@features/vist/models/status-label';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-visit',
  imports: [DatePipe, MatButtonModule, MatDialogModule, MatSnackBarModule, RouterLink],
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
  protected readonly statusLabel = visitStatusLabel;
  readonly selectedStatuses = input<Status[]>(Object.values(Status));
  protected readonly visibleVisits = computed(() => {
    const selectedStatuses = new Set(this.selectedStatuses());
    return this.visit().filter((visit) => selectedStatuses.has(visit.status));
  });
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly canScheduleFollowUp = this.authState.hasRole(Roles.vet);
  protected readonly canSearch = this.authState.hasRole(Roles.vet);
  protected readonly searchText = signal('');
  protected readonly searchPlaceholder = $localize`:@@visits.searchPlaceholder:Rechercher par nom de l'animal, description ou identifiant`;
  private readonly visitSearch = new Subject<string>();

  ngOnInit(): void {
    this.visitSearch
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        startWith(''),
        switchMap((query) => {
          this.visit.set([]);
          this.isLoading.set(true);
          this.errorMessage.set(null);
          const visits = query
            ? this.visitService.searchVisits(query)
            : this.visitService.getAllVisits();
          return visits.pipe(
            finalize(() => this.isLoading.set(false)),
            catchError(() => {
              this.errorMessage.set(
                $localize`:@@visitListLoadError:Unable to load visits. Please try again later`,
              );
              return EMPTY;
            }),
          );
        }),
        this.untilDestroyed,
      )
      .subscribe((visits) =>
        this.visit.update((visit) =>
          [...visit, visits].sort(
            (a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime(),
          ),
        ),
      );
  }

  protected searchVisits(query: string): void {
    this.searchText.set(query);
    this.visitSearch.next(query.trim());
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

  protected openVetNote(visit: Visit): void {
    this.dialog
      .open(VetNoteDialog, {
        data: { visit },
        width: '620px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .subscribe((updatedVisit?: Visit) => {
        if (updatedVisit) {
          this.visit.update((visits) =>
            visits.map((current) =>
              current.visitId === updatedVisit.visitId ? updatedVisit : current,
            ),
          );
          this.snackBar.open(
            $localize`:@@visits.note.confirmation:La note a été enregistrée.`,
            '',
            {
              duration: 3000,
              verticalPosition: 'top',
            },
          );
        }
      });
  }
}
