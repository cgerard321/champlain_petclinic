import { Component, input, output, signal } from '@angular/core';

import { Status } from '@features/vist/models/Status';

@Component({
  selector: 'app-filter-button',
  standalone: true,
  templateUrl: './filter-button.html',
  styleUrl: './filter-button.css',
})

export class FilterButton {
  protected readonly statuses = Object.values(Status) as Status[];
  readonly selectedStatuses = input.required<Status[]>();
  protected readonly isMenuOpen = signal(false);
  readonly statusSelectionChange = output<Status[]>();

  protected toggleMenu(): void {
    this.isMenuOpen.update((isOpen) => !isOpen);
  }

  protected toggleStatus(status: Status): void {
    const selected = new Set(this.selectedStatuses());
    if (selected.has(status)) {
      selected.delete(status);
    } else {
      selected.add(status);
    }

    function isSelected(candidate: Status): boolean {
      return selected.has(candidate);
    }

    this.statusSelectionChange.emit(this.statuses.filter(isSelected));  }


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
}
