import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { clearTranslations, loadTranslations } from '@angular/localize';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SettingsAccount } from '@features/settings/services/settings-account';
import { SettingsFormState } from '@features/settings/services/settings-form-state';

import { Settings } from './settings';

const CURRENT_USER: CurrentUserResponse = {
  userId: '69f852ca-625b-11ee-8c99-0242ac120002',
  email: 'Vet1',
  username: 'Vet1',
  roles: ['VET'],
};

const EN_SETTINGS = {
  'settings.title': 'Settings',
  'settings.security.title': 'Security',
  'settings.display.title': 'Display',
  'settings.save': 'Save',
  'settings.cancel': 'Cancel',
  'settings.security.account.title': 'Account',
  'settings.security.account.username': 'Username',
  'settings.security.account.role': 'Role',
  'settings.security.role.vet': 'Veterinarian',
  'settings.security.username.title': 'Change your username',
  'settings.security.username.label': 'New username',
};

describe('Settings page (VETS-CPC-2089, VETS-CPC-2090)', () => {
  let navigateByUrl: ReturnType<typeof vi.fn>;
  let snackBarOpen: ReturnType<typeof vi.fn>;
  let http: HttpTestingController;

  beforeEach(() => {
    vi.clearAllMocks();
    navigateByUrl = vi.fn();
    snackBarOpen = vi.fn();
  });

  // Translations are installed globally, so a test that loads them has to put the environment
  // back as it found it or it would leak into the next ones.
  afterEach(() => {
    http.verify();
    clearTranslations();
    vi.restoreAllMocks();
  });

  async function render(): Promise<{
    fixture: ComponentFixture<Settings>;
    host: HTMLElement;
  }> {
    await TestBed.configureTestingModule({
      imports: [Settings],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        // Both are route-scoped in production, so the spec provides them the same way.
        SettingsAccount,
        SettingsFormState,
        { provide: Router, useValue: { navigateByUrl } },
        { provide: MatSnackBar, useValue: { open: snackBarOpen } },
        { provide: AuthState, useValue: { logout: vi.fn(() => of(undefined)) } },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);

    const fixture = TestBed.createComponent(Settings);
    await fixture.whenStable();

    // The Security section reads the signed-in employee as soon as it renders.
    http.expectOne('/api/gateway/users/jwt').flush(CURRENT_USER);
    await fixture.whenStable();

    return { fixture, host: fixture.nativeElement as HTMLElement };
  }

  function navItem(host: HTMLElement, label: string): HTMLButtonElement {
    const items = Array.from(host.querySelectorAll<HTMLButtonElement>('.settings-nav-item'));
    const match = items.find((item) => item.textContent?.includes(label));

    if (!match) {
      throw new Error(`No nav item labelled "${label}"`);
    }

    return match;
  }

  function actionButton(host: HTMLElement, label: string): HTMLButtonElement {
    const buttons = Array.from(
      host.querySelectorAll<HTMLButtonElement>('.settings-actions button'),
    );
    const match = buttons.find((button) => button.textContent?.includes(label));

    if (!match) {
      throw new Error(`No action button labelled "${label}"`);
    }

    return match;
  }

  function panelTitle(host: HTMLElement): Element | null {
    return host.querySelector('.settings-panel-title');
  }

  function usernameInput(host: HTMLElement): HTMLInputElement {
    const field = host.querySelector<HTMLInputElement>('.username-field input');

    if (!field) {
      throw new Error('No username input rendered');
    }

    return field;
  }

  async function type(
    fixture: ComponentFixture<Settings>,
    host: HTMLElement,
    value: string,
  ): Promise<void> {
    const field = usernameInput(host);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  // 1. POSITIVE - Opens on Security.
  it('opens on the Security section', async () => {
    // Act
    const { host } = await render();

    // Assert
    expect(navItem(host, 'Sécurité').classList.contains('is-active')).toBe(true);
    expect(navItem(host, 'Affichage').classList.contains('is-active')).toBe(false);
    expect(panelTitle(host)?.textContent).toContain('Sécurité');
  });

  // 2. POSITIVE - Both sections exist.
  // The ticket asks for the page to be clearly split in two, so there must be exactly two
  // entries in the nav - not one, not three.
  it('offers a Security section and a Display section', async () => {
    // Act
    const { host } = await render();

    // Assert
    const items = host.querySelectorAll('.settings-nav-item');
    expect(items).toHaveLength(2);
    expect(host.textContent).toContain('Sécurité');
    expect(host.textContent).toContain('Affichage');
  });

  // 3. POSITIVE - Display swaps the panel and moves the highlight.
  it('switches to Display and moves the highlight onto it', async () => {
    // Arrange
    const { fixture, host } = await render();

    // Act
    navItem(host, 'Affichage').click();
    await fixture.whenStable();

    // Assert
    expect(navItem(host, 'Affichage').classList.contains('is-active')).toBe(true);
    expect(navItem(host, 'Sécurité').classList.contains('is-active')).toBe(false);
    expect(panelTitle(host)?.textContent).toContain('Affichage');
  });

  // 4. VETS-CPC-2090 - Replaces the CPC-2089 test that expected Save to confirm and leave.
  // Save no longer does either: it drives the sections, and criterion 6 keeps it disabled
  // until one of them actually has a valid change.
  it('keeps Save disabled while nothing has changed', async () => {
    // Act
    const { host } = await render();

    // Assert
    expect(actionButton(host, 'Enregistrer').disabled).toBe(true);
  });

  // 5. VETS-CPC-2090 - Criterion 6, the other way round.
  it('enables Save once a section holds a valid change', async () => {
    // Arrange
    const { fixture, host } = await render();

    // Act
    await type(fixture, host, 'jean_dupont');

    // Assert
    expect(actionButton(host, 'Enregistrer').disabled).toBe(false);
  });

  // 6. VETS-CPC-2090 - Replaces the CPC-2089 test that expected Cancel to leave the page.
  // Criterion 2 asks Cancel to restore the current value instead.
  it('restores the current value when cancelling, without leaving the page', async () => {
    // Arrange
    const { fixture, host } = await render();
    await type(fixture, host, 'jean_dupont');

    // Act
    actionButton(host, 'Annuler').click();
    await fixture.whenStable();

    // Assert
    expect(usernameInput(host).value).toBe('Vet1');
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(snackBarOpen).not.toHaveBeenCalled();
  });

  // 7. POSITIVE - Both actions sit at the bottom of the page.
  it('keeps both actions at the bottom of the page', async () => {
    // Act
    const { host } = await render();

    // Assert
    const page = host.querySelector('.settings-page');
    expect(page?.lastElementChild?.classList.contains('settings-actions')).toBe(true);
    expect(actionButton(host, 'Annuler')).toBeTruthy();
    expect(actionButton(host, 'Enregistrer')).toBeTruthy();
  });

  // 8. POSITIVE - Save sends the change through the section.
  it('sends the change through the section when saving', async () => {
    // Arrange
    const { fixture, host } = await render();
    await type(fixture, host, 'jean_dupont');

    // Act
    actionButton(host, 'Enregistrer').click();
    await fixture.whenStable();

    // Assert
    const request = http.expectOne(
      `/api/gateway/users/${CURRENT_USER.userId}/username`,
    );
    expect(request.request.body).toBe('jean_dupont');
    request.flush('jean_dupont');
  });

  // 9. POSITIVE - The whole template switches to English.
  it('renders fully in English once the catalogue is installed', async () => {
    // Arrange
    loadTranslations(EN_SETTINGS);

    // Act
    const { host } = await render();

    // Assert
    expect(host.textContent).toContain('Settings');
    expect(host.textContent).toContain('Security');
    expect(host.textContent).toContain('Display');
    expect(host.textContent).toContain('Save');
    expect(host.textContent).toContain('Cancel');
    expect(host.textContent).toContain('Veterinarian');
    expect(host.textContent).not.toContain('Paramètres');
  });

  // 10. NEGATIVE - Stays French when no catalogue is installed.
  it('stays in French when no catalogue is installed', async () => {
    // Act
    const { host } = await render();

    // Assert
    expect(host.textContent).toContain('Paramètres');
    expect(host.textContent).toContain('Vétérinaire');
    expect(host.textContent).not.toContain('Settings');
  });
});
