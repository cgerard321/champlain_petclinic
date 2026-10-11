import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

@Component({
  selector: 'app-visit',
  styleUrl: './VistListTable.css',
  templateUrl: './VistListTable.html',
})
export class VistListTable implements OnInit {
  private readonly visitService = inject(VisitService);
  private readonly untilDestroyed = takeUntilDestroyed<Visit>();
  protected readonly visit = signal<Visit[]>([]);
  readonly selectedStatuses = input<Status[]>(Object.values(Status));
  protected readonly visibleVisits = computed(() => {
    const selectedStatuses = new Set(this.selectedStatuses());
    return this.visit().filter((visit) => selectedStatuses.has(visit.status));
  });
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

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
          this.errorMessage.set('Unable to load visits. Please try again later');
        },
        complete: () => this.isLoading.set(false),
      });
  }
}
