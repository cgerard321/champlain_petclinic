import { Component, computed, DestroyRef, inject, linkedSignal, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { employeeRoleLabel } from '@features/settings/models/role-label';
import {
  checkUsernameRules,
  isReserved,
  isSameAsCurrent,
  isValidUsername,
  normalizeUsername,
  suggestUsernames,
  UsernameRuleId,
} from '@features/settings/models/username-rules';
import { SettingsAccount } from '@features/settings/services/settings-account';
import {
  SettingsFormState,
  SettingsSectionForm,
} from '@features/settings/services/settings-form-state';

const SECTION_ID = 'username';

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

  protected readonly currentUser = this.account.currentUser;

  // Criterion 1, read-only.
  protected readonly current = computed(() => this.currentUser()?.username ?? '');
  protected readonly roleLabel = computed(() => employeeRoleLabel(this.currentUser()?.roles ?? []));

  // Takes the current name as soon as it arrives over HTTP, but keeps whatever the employee has
  // already typed: on the first pass the previous value is empty, so the current name wins.
  protected readonly draft = linkedSignal<string, string>({
    source: this.current,
    computation: (current, previous) => previous?.value || current,
  });

  protected readonly hasTyped = signal(false);

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  private readonly saved = signal(false);

  // One entry per line, with its own wording, so the template stays dumb.
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

  // The name as it will actually be stored, spaces removed.
  protected readonly preview = computed(() => normalizeUsername(this.draft()));

  //Offered only when the typed name breaks a rule; otherwise it is noise.
  protected readonly suggestions = computed(() =>
    this.valid() ? [] : suggestUsernames(this.draft()),
  );

  protected readonly errorText = computed(() => {
    const failure = this.errorMessage();

    if (failure) {
      return failure;
    }

    if (this.hasTyped() && this.reserved()) {
      return $localize`:@@settings.security.username.reserved:Ce nom est réservé`;
    }

    return '';
  });

  protected readonly showSameAsCurrent = computed(() => this.hasTyped() && this.sameAsCurrent());
  //show is it your actual name
  protected readonly showPreview = computed(
    () => this.hasTyped() && this.valid() && !this.sameAsCurrent(),
  );
  // Criterion 5: shows the name the employee will use at their next login (spaces trimmed).
  protected readonly showSuggestions = computed(
    () => this.hasTyped() && this.suggestions().length > 0,
  );

  // read by the page and by the leave guard.
  readonly isDirty = computed(
    () => !this.saved() && !isSameAsCurrent(this.draft(), this.current()),
  );
  readonly canSave = computed(
    () => !this.saved() && this.valid() && !this.sameAsCurrent() && !this.isSubmitting(),
  );

  constructor() {
    this.formState.register(SECTION_ID, this);
    inject(DestroyRef).onDestroy(() => this.formState.unregister(SECTION_ID));
  }

  protected onType(value: string): void {
    this.hasTyped.set(true);
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
    this.hasTyped.set(false);
    this.errorMessage.set(null);
    this.draft.set(this.current());
  }

  save(): void {
    const userId = this.currentUser()?.userId;
    const username = normalizeUsername(this.draft());

    // Guards itself with the very value the page uses to enable the button, so a direct call
    // can never do something the button would refuse. In particular it refuses a name equal to
    // the current one: that would rename nothing and sign the employee out for no reason.
    if (!userId || !this.canSave()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.account.updateUsername(userId, username).subscribe({
      next: () => this.onSaved(username),
      error: () => {
        this.isSubmitting.set(false);
        // Criterion 7. The same business message whatever the status code: a duplicate comes
        // back as a 500 because of the UNIQUE constraint, and no technical detail is ever
        // shown to the employee.
        this.errorMessage.set(
          $localize`:@@settings.security.username.saveFailed:Ce nom d'utilisateur n'a pas pu être enregistré. Il est peut-être déjà utilisé, essayez un autre nom.`,
        );
      },
    });
  }

  private onSaved(username: string): void {
    this.isSubmitting.set(false);

    // Clears the unsaved-changes state BEFORE navigating. The token still carries the old
    // name, so `current()` cannot be refreshed here, and without this the leave guard would
    // ask the employee to confirm losing changes that were just saved.
    this.saved.set(true);
    this.hasTyped.set(false);
    this.draft.set(username);

    this.snackBar.open(
      $localize`:@@settings.security.username.saved:Nom d'utilisateur modifié`,
      '',
      { duration: 2000, verticalPosition: 'top' },
    );

    // The token still holds the old name, so staying signed in is not an option. Navigate even
    // if the logout call fails, for the same reason.
    this.authState.logout().subscribe({
      next: () => void this.router.navigateByUrl('/login'),
      error: () => void this.router.navigateByUrl('/login'),
    });
  }
}
