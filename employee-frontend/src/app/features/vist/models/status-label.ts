import { Status } from '@features/vist/models/Status';

export function visitStatusLabel(status: Status): string {
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
