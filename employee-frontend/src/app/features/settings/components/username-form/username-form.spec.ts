import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SettingsAccount } from '@features/settings/services/settings-account';
import { SettingsFormState } from '@features/settings/services/settings-form-state';

import { UsernameForm } from './username-form';

const USER_ID = '69f852ca-625b-11ee-8c99-0242ac120002';

const CURRENT_USER: CurrentUserResponse = {
  userId: USER_ID,
  email: 'Vet1',
  username: 'Vet1',
  roles: ['VET'],
};

describe('UsernameForm (VETS-CPC-2090)', () => {
  let fixture: ComponentFixture<UsernameForm>;
  let host: HTMLElement;
  let http: HttpTestingController;
  let formState: SettingsFormState;
  let navigateByUrl: ReturnType<typeof vi.fn>;
  let snackBarOpen: ReturnType<typeof vi.fn>;
  let logout: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    vi.clearAllMocks();
    navigateByUrl = vi.fn();
    snackBarOpen = vi.fn();
    logout = vi.fn(() => of(undefined));

    await TestBed.configureTestingModule({
      imports: [UsernameForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        SettingsAccount,
        SettingsFormState,
        { provide: Router, useValue: { navigateByUrl } },
        { provide: MatSnackBar, useValue: { open: snackBarOpen } },
        { provide: AuthState, useValue: { logout } },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    formState = TestBed.inject(SettingsFormState);
    fixture = TestBed.createComponent(UsernameForm);
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
  });


  async function signedIn(user: CurrentUserResponse = CURRENT_USER): Promise<void> {
    http.expectOne('/api/gateway/users/jwt').flush(user);
    await fixture.whenStable();
    host = fixture.nativeElement as HTMLElement;
  }

  function input(): HTMLInputElement {
    const field = host.querySelector<HTMLInputElement>('.username-field input');

    if (!field) {
      throw new Error('No username input rendered');
    }

    return field;
  }

  async function type(value: string): Promise<void> {
    const field = input();
    field.value = value;
    field.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  function ruleLabels(met: boolean): string[] {
    return Array.from(host.querySelectorAll('.rule'))
      .filter((rule) => rule.classList.contains('is-met') === met)
      .map((rule) => rule.textContent?.trim() ?? '');
  }

  function suggestions(): HTMLButtonElement[] {
    return Array.from(host.querySelectorAll<HTMLButtonElement>('.suggestion-list button'));
  }

  function liveErrorRegion(): Element {
    const region = host.querySelector('.field-note.is-error');

    if (!region) {
      throw new Error('No error live region rendered');
    }

    return region;
  }

  // CRITERION 1. POSITIVE - The account block shows the username and a readable role.
  it('shows the current username and the translated role', async () => {
    // Act
    await signedIn();

    // Assert
    const facts = host.querySelector('.account-facts')?.textContent ?? '';
    expect(facts).toContain('Vet1');
    expect(facts).toContain('Vétérinaire');
    // The raw role from the token must never reach the screen.
    expect(facts).not.toContain('VET');
  });

  // POSITIVE - The field starts on the current name, so Save is off until something changes.
  it('prefills the field with the current username', async () => {
    // Act
    await signedIn();

    // Assert
    expect(input().value).toBe('Vet1');
  });

  // NEGATIVE - A slow answer must not wipe what the employee already typed.
  it('does not overwrite a name typed before the server answered', async () => {
    // Arrange
    host = fixture.nativeElement as HTMLElement;

    // Act
    await type('jean_dupont');
    await signedIn();

    // Assert
    expect(input().value).toBe('jean_dupont');
  });

  // CRITERION 3. POSITIVE - Every rule is listed, and ticked as it holds.
  it('lists the five rules and ticks them in real time', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('a');

    // Assert
    expect(host.querySelectorAll('.rule')).toHaveLength(5);
    expect(ruleLabels(false).join(' ')).toContain('3 à 30 caractères');

    // Act
    await type('jean_dupont');

    // Assert
    expect(ruleLabels(true)).toHaveLength(5);
  });

  // CRITERION 3. NEGATIVE - Says WHICH rule is broken, not just that one is.
  it('leaves only the character rule unticked on a name with a period', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('jean.tremblay');

    // Assert
    expect(ruleLabels(false)).toHaveLength(1);
    expect(ruleLabels(false)[0]).toContain('Lettres sans accents');
  });

  // CRITERION 4. POSITIVE - Retyping the current name says so, case aside.
  it('says the name is the same as the current one', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('VET1');

    // Assert
    expect(host.textContent).toContain('identique au nom actuel');
  });

  // CRITERION 5. POSITIVE - Previews the name used at the next sign-in, spaces removed.
  it('previews the username of the next sign-in', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('  jean_dupont  ');

    // Assert
    const preview = host.querySelector('.is-preview')?.textContent ?? '';
    expect(preview).toContain('jean_dupont');
    expect(preview).not.toContain('  jean_dupont  ');
  });

  // CRITERION 9. POSITIVE - Up to three suggestions, and clicking one fills the field.
  it('suggests corrected names and fills the field when one is clicked', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('jean.tremblay');

    // Assert
    expect(suggestions().length).toBeGreaterThan(0);
    expect(suggestions().length).toBeLessThanOrEqual(3);

    // Act
    const first = suggestions()[0];
    const chosen = first?.textContent?.trim() ?? '';
    first?.click();
    await fixture.whenStable();

    // Assert
    expect(input().value).toBe(chosen);
  });

  // CRITERION 9. NEGATIVE - No suggestions on a name that already passes.
  it('offers no suggestion once the name is valid', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('jean_dupont');

    // Assert
    expect(suggestions()).toHaveLength(0);
  });

  // RULE 8. NEGATIVE - A reserved name gets its own message.
  it('refuses a reserved name with a message of its own', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('admin');

    // Assert
    expect(host.textContent).toContain('Ce nom est réservé');
    expect(fixture.componentInstance.canSave()).toBe(false);
  });

  // RULE 7. POSITIVE - Spaces are removed on blur, not while typing.
  it('keeps spaces while typing and removes them on blur', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('  jean_dupont  ');

    // Assert
    expect(input().value).toBe('  jean_dupont  ');

    // Act
    input().dispatchEvent(new Event('blur'));
    await fixture.whenStable();

    // Assert
    expect(input().value).toBe('jean_dupont');
  });

  // CRITERION 6. NEGATIVE - Nothing to save on arrival, nor on an invalid name.
  it('cannot be saved before anything changes, nor on an invalid name', async () => {
    // Arrange
    await signedIn();

    // Assert
    expect(fixture.componentInstance.canSave()).toBe(false);

    // Act
    await type('jean.tremblay');

    // Assert
    expect(fixture.componentInstance.canSave()).toBe(false);
  });

  // CRITERION 6. POSITIVE - A valid change opens the gate, and registers with the page.
  it('can be saved once the name is valid and different', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('jean_dupont');

    // Assert
    expect(fixture.componentInstance.canSave()).toBe(true);
    expect(formState.canSave()).toBe(true);
    expect(formState.isDirty()).toBe(true);
  });

  // CRITERION 2. POSITIVE - Cancel puts the current value back.
  it('restores the current username when cancelled', async () => {
    // Arrange
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.cancel();
    await fixture.whenStable();

    // Assert
    expect(input().value).toBe('Vet1');
    expect(formState.isDirty()).toBe(false);
  });

  // CRITERION 8. POSITIVE - Confirms, signs out, and sends the employee to the login page.
  it('confirms then signs the employee out after a successful change', async () => {
    // Arrange
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.save();
    http.expectOne(`/api/gateway/users/${USER_ID}/username`).flush('jean_dupont');
    await fixture.whenStable();

    // Assert
    expect(snackBarOpen).toHaveBeenCalledWith("Nom d'utilisateur modifié", '', {
      duration: 2000,
      verticalPosition: 'top',
    });
    expect(logout).toHaveBeenCalledTimes(1);
    expect(navigateByUrl).toHaveBeenCalledWith('/login');
  });

  // CRITERIA 8 and 17. POSITIVE - Nothing is left dirty, or the leave guard would ask the
  // employee to confirm losing changes that were just saved.
  it('leaves no unsaved change behind after a successful save', async () => {
    // Arrange
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.save();
    http.expectOne(`/api/gateway/users/${USER_ID}/username`).flush('jean_dupont');
    await fixture.whenStable();

    // Assert
    expect(formState.isDirty()).toBe(false);
    expect(formState.canSave()).toBe(false);
  });

  // CRITERION 8. POSITIVE - A failing logout still sends the employee to the login page,
  // because the token carries a username that no longer exists.
  it('reaches the login page even when signing out fails', async () => {
    // Arrange
    logout.mockReturnValue(throwError(() => new Error('offline')));
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.save();
    http.expectOne(`/api/gateway/users/${USER_ID}/username`).flush('jean_dupont');
    await fixture.whenStable();

    // Assert
    expect(navigateByUrl).toHaveBeenCalledWith('/login');
  });

  // CRITERION 7. NEGATIVE - A refused name shows a business message, never the raw error.
  it('shows a business message when the save fails', async () => {
    // Arrange
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.save();
    http
      .expectOne(`/api/gateway/users/${USER_ID}/username`)
      .flush('Duplicate entry', { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    // Assert
    expect(host.textContent).toContain('peut-être déjà utilisé');
    expect(host.textContent).not.toContain('Duplicate entry');
    expect(host.textContent).not.toContain('500');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  // CRITERION 6. NEGATIVE - A second click cannot fire a second request.
  it('ignores a save while one is already in flight', async () => {
    // Arrange
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.save();
    fixture.componentInstance.save();

    // Assert
    http.expectOne(`/api/gateway/users/${USER_ID}/username`).flush('jean_dupont');
    await fixture.whenStable();
  });

  // CRITERION 6 / BUG 2. NEGATIVE - Saving the name already in use must send nothing.
  // Without this guard the employee would be renamed to the name they already have, signed
  // out, and sent to the login page for no reason at all.
  it('sends nothing when the field still holds the current name', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('Vet1');
    fixture.componentInstance.save();

    // Assert
    http.expectNone(`/api/gateway/users/${USER_ID}/username`);
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(snackBarOpen).not.toHaveBeenCalled();
  });

  // CRITERION 4 / BUG 2. NEGATIVE - Same thing when only the case differs.
  it('sends nothing when the name differs from the current one only by its case', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('VET1');
    fixture.componentInstance.save();

    // Assert
    http.expectNone(`/api/gateway/users/${USER_ID}/username`);
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  // CRITERION 6 / BUG 3. POSITIVE - A trailing space must not hold the button back.
  // Rule 7 trims on blur and on submit, so the gate judges the name that will actually be
  // sent. Judging the raw value would disable Save on a pasted name with nothing on screen
  // telling the employee to leave the field.
  it('can be saved with a trailing space, before the field loses focus', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('jean_dupont ');

    // Assert
    expect(fixture.componentInstance.canSave()).toBe(true);
    expect(formState.canSave()).toBe(true);
  });

  // RULE 7. POSITIVE - And the spaces never reach the server.
  it('sends the trimmed name', async () => {
    // Arrange
    await signedIn();
    await type('  jean_dupont  ');

    // Act
    fixture.componentInstance.save();

    // Assert
    const request = http.expectOne(`/api/gateway/users/${USER_ID}/username`);
    expect(request.request.body).toBe('jean_dupont');
    request.flush('jean_dupont');
    await fixture.whenStable();
  });

  // CRITERION 19. POSITIVE - The field is tied to the checklist for screen readers.
  it('links the field to the rule checklist', async () => {
    // Act
    await signedIn();

    // Assert
    expect(input().getAttribute('aria-describedby')).toBe('username-rules');
    expect(host.querySelector('#username-rules')).toBeTruthy();
  });

  // CRITERION 19. POSITIVE - The live region exists before it has anything to announce.
  // A region inserted at the same time as its content is announced unreliably, so it must be
  // in the DOM from the start, merely empty.
  it('keeps an empty error live region on screen before any error', async () => {
    // Act
    await signedIn();

    // Assert
    const region = liveErrorRegion();
    expect(region.getAttribute('role')).toBe('alert');
    expect(region.getAttribute('aria-live')).toBe('assertive');
    expect(region.textContent?.trim()).toBe('');
  });

  // CRITERION 19. POSITIVE - Both kinds of problem are announced through that region.
  it('announces a reserved name in the error live region', async () => {
    // Arrange
    await signedIn();

    // Act
    await type('admin');

    // Assert
    expect(liveErrorRegion().textContent).toContain('Ce nom est réservé');
  });

  it('announces a failed save in the error live region', async () => {
    // Arrange
    await signedIn();
    await type('jean_dupont');

    // Act
    fixture.componentInstance.save();
    http
      .expectOne(`/api/gateway/users/${USER_ID}/username`)
      .flush('Duplicate entry', { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    // Assert
    expect(liveErrorRegion().textContent).toContain('peut-être déjà utilisé');
  });

  // NEGATIVE - Never sends anything when the account could not be read.
  it('does not save when the signed-in employee is unknown', async () => {
    // Arrange
    http.expectOne('/api/gateway/users/jwt').flush('no token', {
      status: 401,
      statusText: 'Unauthorized',
    });
    await fixture.whenStable();
    host = fixture.nativeElement as HTMLElement;

    // Act
    await type('jean_dupont');
    fixture.componentInstance.save();

    // Assert
    http.expectNone(`/api/gateway/users/${USER_ID}/username`);
  });
});
