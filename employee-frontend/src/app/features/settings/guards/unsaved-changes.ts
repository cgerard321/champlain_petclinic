import { inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CanDeactivateFn } from '@angular/router';
import { map, Observable } from 'rxjs';

import { HasUnsavedChanges } from '@core/models/has-unsaved-changes';
import { ConfirmDialog, ConfirmDialogData } from '@shared/components/confirm-dialog/confirm-dialog';

export const unsavedChangesGuard: CanDeactivateFn<HasUnsavedChanges> = (
  component,
): boolean | Observable<boolean> => {
  if (!component.isDirty()) {
    return true;
  }

  const data: ConfirmDialogData = {
    title: $localize`:@@settings.leave.title:Modifications non enregistrées`,
    message: $localize`:@@settings.leave.message:Vos modifications ne sont pas enregistrées. Voulez-vous quitter la page ?`,
    confirmLabel: $localize`:@@settings.leave.confirm:Quitter`,
    cancelLabel: $localize`:@@settings.leave.cancel:Rester`,
  };

  return inject(MatDialog)
    .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, { data })
    .afterClosed()
    .pipe(map((confirmed) => confirmed === true));
};
