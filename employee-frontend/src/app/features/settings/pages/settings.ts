import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIcon } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { HasUnsavedChanges } from '@core/models/has-unsaved-changes';
import { Display } from '@features/settings/components/display/display';
import { PasswordReset } from '@features/settings/components/password-reset/password-reset';
import { UsernameForm } from '@features/settings/components/username-form/username-form';
import { SettingsFormState } from '@features/settings/services/settings-form-state';
import { ThemeService } from '@features/settings/services/themeservice';

type SettingsSection = 'security' | 'display';
@Component({
  selector: 'app-settings',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatSnackBarModule,
    MatIcon,
    Display,
    PasswordReset,
    UsernameForm,
  ],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings implements OnInit, HasUnsavedChanges {
  private readonly formState = inject(SettingsFormState);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly themeService = inject(ThemeService);

  protected readonly activeSection = signal<SettingsSection>('security');
  private originalThemeState = false;

  ngOnInit(): void {
    this.originalThemeState = this.themeService.isDarkMode();
  }

  protected readonly themeChanged = computed(
    () => this.themeService.isDarkMode() !== this.originalThemeState,
  );

  protected readonly canSave = computed(() => this.formState.canSave() || this.themeChanged());
  readonly isDirty = this.formState.isDirty;

  protected select(section: SettingsSection): void {
    if (section === this.activeSection()) {
      return;
    }

    this.activeSection.set(section);
  }

  protected close(): void {
    if (this.themeChanged()) {
      this.themeService.toggleTheme();
    }
    void this.router.navigateByUrl('/home');
  }

  protected save(): void {
    this.themeService.saveThemePreference();
    this.snackBar.open($localize`:@@settings.saved:Modifications enregistrées`, '', {
      duration: 2000,
      verticalPosition: 'top',
    });
    this.formState.saveAll();
    this.router.navigateByUrl('/home');
  }

  protected cancel(): void {
    if (this.themeService.isDarkMode() !== this.originalThemeState) {
      this.themeService.toggleTheme();
    }
    this.formState.cancelAll();
    this.router.navigateByUrl('/home');
  }
}
