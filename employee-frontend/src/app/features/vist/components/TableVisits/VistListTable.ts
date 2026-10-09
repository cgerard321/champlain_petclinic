import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

@Component({
  selector: 'app-visit',
  styleUrl: './VistListTable.css',
  templateUrl: './VistListTable.html',
  imports: [DatePipe],
})
export class VistListTable implements OnInit {
  private readonly visitService = inject(VisitService);
  private readonly untilDestroyed = takeUntilDestroyed<Visit>();
  protected readonly visit = signal<Visit[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected statusLabel(status: Status): string {
    switch (status) {
      case Status.SCHEDULED:
        return $localize`:@@visits.statusScheduled:Planifiée`;
      case Status.COMPLETED:
        return $localize`:@@visits.statusCompleted:Terminée`;
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
}
