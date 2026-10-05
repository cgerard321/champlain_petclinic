import { ComponentFixture, TestBed } from '@angular/core/testing';
import { clearTranslations, loadTranslations } from '@angular/localize';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { vi } from 'vitest';

import { Settings } from './settings';

const EN_SETTINGS = {
  'settings.title': 'Settings',
  'settings.security.title': 'Security',
  'settings.display.title': 'Display',
  'settings.save': 'Save',
  'settings.cancel': 'Cancel',
  'settings.saved': 'Changes saved',
};

describe('Settings page (VETS-CPC-2089)', () => {
  let navigateByUrl: ReturnType<typeof vi.fn>;
  let snackBarOpen: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.clearAllMocks();
    navigateByUrl = vi.fn();
    snackBarOpen = vi.fn();
  });

  // Translations are installed globally, so a test that loads them has to put the environment
  // back as it found it or it would leak into the next ones.
  afterEach(() => {
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
        { provide: Router, useValue: { navigateByUrl } },
        { provide: MatSnackBar, useValue: { open: snackBarOpen } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Settings);
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

  // 4. POSITIVE - Save confirms with a snack bar and leaves the page.

  it('confirms with a snack bar and leaves the page when saving', async () => {
    // Arrange
    const { host } = await render();

    // Act
    actionButton(host, 'Enregistrer').click();

    // Assert
    expect(snackBarOpen).toHaveBeenCalledWith('Modifications enregistrées', '', {
      duration: 2000,
      verticalPosition: 'top',
    });
    expect(navigateByUrl).toHaveBeenCalledWith('/home');
  });

  // 6. POSITIVE - Cancel leaves the page without confirming.

  it('leaves the page without confirming anything when cancelling', async () => {
    // Arrange
    const { host } = await render();

    // Act
    actionButton(host, 'Annuler').click();

    // Assert
    expect(navigateByUrl).toHaveBeenCalledWith('/home');
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

  // 8. POSITIVE - The whole template switches to English.

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
    expect(host.textContent).not.toContain('Paramètres');
  });

  // 9. POSITIVE - The snack bar message switches to English too.
  it('translates the snack bar message as well', async () => {
    // Arrange
    loadTranslations(EN_SETTINGS);
    const { host } = await render();

    // Act
    actionButton(host, 'Save').click();

    // Assert
    expect(snackBarOpen).toHaveBeenCalledWith('Changes saved', '', {
      duration: 2000,
      verticalPosition: 'top',
    });
  });

  // 10. NEGATIVE - Stays French when no catalogue is installed.
  it('stays in French when no catalogue is installed', async () => {
    // Act
    const { host } = await render();

    // Assert
    expect(host.textContent).toContain('Paramètres');
    expect(host.textContent).not.toContain('Settings');
  });
});
