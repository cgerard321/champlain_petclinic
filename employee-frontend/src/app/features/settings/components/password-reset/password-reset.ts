import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { SettingsAccount } from '@features/settings/services/settings-account';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const COOLDOWN_SECONDS = 60;

@Component({
  selector: 'app-password-reset',
  imports: [MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './password-reset.html',
  styleUrl: './password-reset.css',
})
export class PasswordReset {
  private readonly account = inject(SettingsAccount);

  protected readonly email = signal('');

  protected readonly sending = signal(false);
  protected readonly cooldownRemaining = signal(0);
  protected readonly notice = signal('');

  protected readonly emailValid = computed(() => EMAIL_PATTERN.test(this.email().trim()));
  protected readonly canSend = computed(
    () => this.emailValid() && !this.sending() && this.cooldownRemaining() === 0,
  );

  private intervalId: ReturnType<typeof setInterval> | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopCooldown());
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
    this.stopCooldown();
    this.cooldownRemaining.set(COOLDOWN_SECONDS);

    this.intervalId = setInterval(() => {
      this.cooldownRemaining.update((seconds) => Math.max(0, seconds - 1));

      if (this.cooldownRemaining() === 0) {
        this.stopCooldown();
      }
    }, 1000);
  }

  private stopCooldown(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }
}
