import { Routes } from '@angular/router';

import { unsavedChangesGuard } from '@features/settings/guards/unsaved-changes';
import { Settings } from '@features/settings/pages/settings';
import { SettingsFormState } from '@features/settings/services/settings-form-state';

export default [
  {
    path: '',
    component: Settings,
    providers: [SettingsFormState],
    canDeactivate: [unsavedChangesGuard],
  },
] satisfies Routes;
