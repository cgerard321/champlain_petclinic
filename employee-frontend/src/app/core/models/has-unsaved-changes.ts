import { Signal } from '@angular/core';

export interface HasUnsavedChanges {
  isDirty: Signal<boolean>;
}
