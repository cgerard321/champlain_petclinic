import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { SettingsAccount } from '@features/settings/services/settings-account';
import {
  SettingsFormState,
  SettingsSectionForm,
} from '@features/settings/services/settings-form-state';

const SECTION_ID = 'password';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const COOLDOWN_SECONDS = 30;
const COOLDOWN_STORAGE_KEY = 'settings.password.cooldownUntil';

@Component({
  selector: 'app-password-reset',
  imports: [MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './password-reset.html',
  styleUrl: './password-reset.css',
})
export class PasswordReset implements SettingsSectionForm {
  private readonly account = inject(SettingsAccount);
  private readonly formState = inject(SettingsFormState);

  protected readonly email = signal('');

  protected readonly sending = signal(false);
  protected readonly cooldownRemaining = signal(0);
  protected readonly notice = signal('');

  protected readonly emailValid = computed(() => EMAIL_PATTERN.test(this.email().trim()));
  protected readonly canSend = computed(
    () => this.emailValid() && !this.sending() && this.cooldownRemaining() === 0,
  );

  readonly isDirty = computed(() => this.email().trim().length > 0);
  readonly canSave = computed(() => false);

  private intervalId: ReturnType<typeof setInterval> | null = null;

  constructor() {
    this.formState.register(SECTION_ID, this);
    this.syncCooldown();

    inject(DestroyRef).onDestroy(() => {
      this.formState.unregister(SECTION_ID);
      this.stopCooldown();
    });
  }

  save(): void {
    return;
  }

  cancel(): void {
    this.email.set('');
    this.notice.set('');
  }

  protected onType(value: string): void {
    this.email.set(value);
  }

  protected onBlur(): void {
    this.email.set(this.email().trim());
  }

  protected send(): void {
    if (!this.canSend()) {
      return;
    }

    const message = $localize`:@@settings.security.password.sent:Si ce courriel correspond à un compte, un lien de réinitialisation a été envoyé.`;

    this.sending.set(true);
    this.startCooldown();

    this.account.sendPasswordResetLink(this.email().trim()).subscribe({
      next: () => {
        this.sending.set(false);
        this.notice.set(message);
      },
      error: () => {
        this.sending.set(false);
        this.notice.set(message);
      },
    });
  }

  private startCooldown(): void {
    this.writeCooldownUntil(Date.now() + COOLDOWN_SECONDS * 1000);
    this.syncCooldown();
  }

  private syncCooldown(): void {
    const remaining = Math.max(0, Math.ceil((this.readCooldownUntil() - Date.now()) / 1000));

    this.cooldownRemaining.set(remaining);

    if (remaining === 0) {
      this.stopCooldown();

      return;
    }

    if (this.intervalId === null) {
      this.intervalId = setInterval(() => this.syncCooldown(), 1000);
    }
  }

  private stopCooldown(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  private readCooldownUntil(): number {
    try {
      return Number(localStorage.getItem(COOLDOWN_STORAGE_KEY)) || 0;
    } catch {
      return 0;
    }
  }

  private writeCooldownUntil(value: number): void {
    try {
      localStorage.setItem(COOLDOWN_STORAGE_KEY, String(value));
    } catch {
      return;
    }
  }
}
