import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { UsernameForm } from '@features/settings/components/username-form/username-form';
import { SettingsFormState } from '@features/settings/services/settings-form-state';

type SettingsSection = 'security' | 'display';

@Component({
  selector: 'app-settings',
  imports: [MatButtonModule, MatCardModule, UsernameForm],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
  private readonly formState = inject(SettingsFormState);

  protected readonly activeSection = signal<SettingsSection>('security');

  protected readonly canSave = this.formState.canSave;

  readonly isDirty = this.formState.isDirty;

  protected select(section: SettingsSection): void {
    if (section === this.activeSection()) {
      return;
    }

    this.activeSection.set(section);
  }

  protected save(): void {
    this.formState.saveAll();
  }

  protected cancel(): void {
    this.formState.cancelAll();
  }
}
