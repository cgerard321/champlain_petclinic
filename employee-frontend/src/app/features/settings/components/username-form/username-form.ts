import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import {
  checkUsernameRules,
  isReserved,
  isSameAsCurrent,
  isValidUsername,
  normalizeUsername,
  suggestAlternatives,
  suggestUsernames,
  UsernameRuleId,
} from '@features/settings/models/username-rules';
import { SettingsAccount } from '@features/settings/services/settings-account';
import {
  SettingsFormState,
  SettingsSectionForm,
} from '@features/settings/services/settings-form-state';
import { employeeRoleLabel } from '@shared/models/role-label';

const SECTION_ID = 'username';

const SAVED_SNACK_BAR: MatSnackBarConfig = { duration: 2000, verticalPosition: 'top' };

function usernameRuleLabel(id: UsernameRuleId): string {
  switch (id) {
    case 'length':
      return $localize`:@@settings.security.username.rules.length:3 à 30 caractères`;
    case 'startsAlpha':
      return $localize`:@@settings.security.username.rules.startsAlpha:Commence par une lettre`;
    case 'charset':
      return $localize`:@@settings.security.username.rules.charset:Lettres sans accents, chiffres ou soulignement seulement`;
    case 'noSpaceNoAt':
      return $localize`:@@settings.security.username.rules.noSpaceNoAt:Pas d'espace ni de « @ »`;
    case 'noTrailing':
      return $localize`:@@settings.security.username.rules.noTrailing:Ne finit pas par un soulignement, et pas deux de suite`;
  }
}

@Component({
  selector: 'app-username-form',
  imports: [MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './username-form.html',
  styleUrl: './username-form.css',
})
export class UsernameForm implements SettingsSectionForm {
  private readonly account = inject(SettingsAccount);
  private readonly formState = inject(SettingsFormState);
  private readonly authState = inject(AuthState);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly currentUser = toSignal(this.account.getCurrentUser(), { initialValue: null });

  protected readonly current = computed(() => this.currentUser()?.username ?? '');
  protected readonly roleLabel = computed(() => employeeRoleLabel(this.currentUser()?.roles ?? []));

  protected readonly draft = signal('');

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly rules = computed(() =>
    checkUsernameRules(this.draft()).map((rule) => ({
      id: rule.id,
      met: rule.met,
      label: usernameRuleLabel(rule.id),
    })),
  );

  protected readonly valid = computed(() => isValidUsername(this.draft()));
  protected readonly reserved = computed(() => isReserved(this.draft()));
  protected readonly sameAsCurrent = computed(() => isSameAsCurrent(this.draft(), this.current()));
  protected readonly preview = computed(() => normalizeUsername(this.draft()));

  protected readonly suggestions = computed(() => {
    if (this.errorMessage()) {
      return suggestAlternatives(this.draft());
    }

    return this.valid() ? [] : suggestUsernames(this.draft());
  });

  protected readonly suggestionsLabel = computed(() =>
    this.errorMessage()
      ? $localize`:@@settings.security.username.tryInstead:Essayez plutôt`
      : $localize`:@@settings.security.username.suggestions:Suggestions`,
  );

  protected readonly errorText = computed(() => {
    const failure = this.errorMessage();

    if (failure) {
      return failure;
    }

    if (this.reserved()) {
      return $localize`:@@settings.security.username.reserved:Ce nom est réservé`;
    }

    return '';
  });

  protected readonly filled = computed(() => normalizeUsername(this.draft()).length > 0);
  protected readonly showSameAsCurrent = computed(() => this.filled() && this.sameAsCurrent());
  protected readonly showPreview = computed(() => this.valid() && !this.sameAsCurrent());

  readonly isDirty = this.filled;
  readonly canSave = computed(
    () =>
      this.currentUser() !== null && this.valid() && !this.sameAsCurrent() && !this.isSubmitting(),
  );

  constructor() {
    this.formState.register(SECTION_ID, this);
    inject(DestroyRef).onDestroy(() => this.formState.unregister(SECTION_ID));
  }

  protected onType(value: string): void {
    this.errorMessage.set(null);
    this.draft.set(value);
  }

  protected onBlur(): void {
    this.draft.set(normalizeUsername(this.draft()));
  }

  protected applySuggestion(suggestion: string): void {
    this.onType(suggestion);
  }

  cancel(): void {
    this.errorMessage.set(null);
    this.draft.set('');
  }

  save(): void {
    const userId = this.currentUser()?.userId;
    const username = normalizeUsername(this.draft());

    if (!userId || !this.canSave()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.account.updateUsername(userId, username).subscribe({
      next: () => this.onSaved(),
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set(
          $localize`:@@settings.security.username.saveFailed:Ce nom d'utilisateur n'a pas pu être enregistré. Il est peut-être déjà utilisé, essayez un autre nom.`,
        );
      },
    });
  }

  private onSaved(): void {
    this.isSubmitting.set(false);
    this.draft.set('');

    this.snackBar.open(
      $localize`:@@settings.security.username.saved:Nom d'utilisateur modifié`,
      '',
      SAVED_SNACK_BAR,
    );

    this.authState.logout().subscribe({
      next: () => void this.router.navigateByUrl('/login'),
      error: () => void this.router.navigateByUrl('/login'),
    });
  }
}
