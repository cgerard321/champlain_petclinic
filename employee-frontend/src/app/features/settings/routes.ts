import { Routes } from '@angular/router';

import { Settings } from '@features/settings/pages/settings';
import { SettingsAccount } from '@features/settings/services/settings-account';
import { SettingsFormState } from '@features/settings/services/settings-form-state';

export default [
  {
    path: '',
    component: Settings,
    providers: [SettingsAccount, SettingsFormState],
  },
] satisfies Routes;
