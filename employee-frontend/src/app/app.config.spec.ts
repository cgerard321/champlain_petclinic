import { TestBed } from '@angular/core/testing';
import { clearTranslations } from '@angular/localize';
import { vi } from 'vitest';

import { Footer } from '@layout/footer/footer';

import { flattenTranslations, getSavedLang, loadActiveTranslations } from './app.config';

/**
 * Mirrors the real en.json shape ({ locale, translations }) so the tests also check that
 * the loader unwraps `translations` before installing it.
 */
const EN_CATALOGUE = {
  locale: 'en',
  translations: {
    'footer.brand': 'PetClinic Employee Portal',
    'footer.privacy': 'Privacy Policy',
    'footer.terms': 'Terms of Service',
    'footer.support': 'Support',
    'footer.copyright': '© 2026 Champlain Pet Clinic. All rights reserved.',
  },
};

describe('Translation loader (app.config)', () => {
  let fetchMock: ReturnType<typeof vi.fn>;
  //prepare the tests by
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();

    fetchMock = vi.fn();
    //create mock of the fetched data
    vi.stubGlobal('fetch', fetchMock);

    //silencing the console error
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
  });
  //cleaning all the data for the next tests
  afterEach(() => {
    clearTranslations();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    localStorage.clear();
    document.documentElement.lang = '';
  });
  //showing the footer in a fake navigator it shows the test if the text is in french or in english
  async function renderFooter(): Promise<HTMLElement> {
    await TestBed.configureTestingModule({ imports: [Footer] }).compileComponents();
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    return fixture.nativeElement as HTMLElement;
  }

  // POSITIVE - A first-time visitor with no saved language gets French and nothing is downloaded.
  it('falls back to the source locale when nothing has been stored', async () => {
    // Assert - the default every first-time visitor gets
    expect(getSavedLang()).toBe('fr');

    // Act
    await loadActiveTranslations();

    // Assert - French is the source locale, so there is nothing to download
    expect(fetchMock).not.toHaveBeenCalled();
    expect(document.documentElement.lang).toBe('fr');
  });

  // POSITIVE - getSavedLang() returns the language the user previously picked in the header.
  it('returns the language the user stored', () => {
    // Arrange
    localStorage.setItem('lang', 'en');

    // Assert
    expect(getSavedLang()).toBe('en');
  });

  // POSITIVE - When English is active, en.json is fetched, installed, and the footer renders in English.
  it('fetches the catalogue and installs it when English is active', async () => {
    // Arrange
    localStorage.setItem('lang', 'en');
    fetchMock.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(EN_CATALOGUE),
    });

    // Act
    await loadActiveTranslations();

    // Assert - the happy path of the whole ticket
    expect(fetchMock).toHaveBeenCalledWith('/i18n/en.json');
    expect(document.documentElement.lang).toBe('en');

    const footer = await renderFooter();
    expect(footer.textContent).toContain('PetClinic Employee Portal');
    expect(footer.textContent).not.toContain('Portail Employé Clinique Vétérinaire');
  });

  // NEGATIVE - If the server returns an error status, the error is logged and the app stays in French.
  it('keeps the app in French when the catalogue responds with an error status', async () => {
    // Arrange
    localStorage.setItem('lang', 'en');
    fetchMock.mockResolvedValue({
      ok: false,
      status: 404,
      json: () => Promise.resolve({}),
    });

    // Act - must resolve, never reject: a failed download cannot block the bootstrap
    await expect(loadActiveTranslations()).resolves.toBeUndefined();

    // Assert
    expect(console.error).toHaveBeenCalled();

    const footer = await renderFooter();
    expect(footer.textContent).toContain('Portail Employé Clinique Vétérinaire');
  });

  // NEGATIVE - If the response body is not valid JSON, the error is logged and the app stays in French.
  it('keeps the app in French when the catalogue is not valid JSON', async () => {
    // Arrange - this is what a missing en.json really looks like in production: nginx serves
    // index.html for unknown paths, so the response is a 200 whose body is HTML
    localStorage.setItem('lang', 'en');
    fetchMock.mockResolvedValue({
      ok: true,
      json: () => Promise.reject(new SyntaxError('Unexpected token <')),
    });

    // Act
    await expect(loadActiveTranslations()).resolves.toBeUndefined();

    // Assert
    expect(console.error).toHaveBeenCalled();

    const footer = await renderFooter();
    expect(footer.textContent).toContain('Portail Employé Clinique Vétérinaire');
  });

  // POSITIVE - When the user explicitly chose French, the loader makes no network call.
  it('does not download anything for a language other than English', async () => {
    // Arrange
    localStorage.setItem('lang', 'fr');

    // Act
    await loadActiveTranslations();

    // Assert - guards the early return that keeps the source locale free of network calls
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('installs a catalogue that is grouped by feature', async () => {
    // Arrange - the real shape of en.json, nested rather than flat
    localStorage.setItem('lang', 'en');
    fetchMock.mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          locale: 'en',
          translations: {
            footer: {
              brand: 'PetClinic Employee Portal',
              privacy: 'Privacy Policy',
              terms: 'Terms of Service',
              support: 'Support',
              copyright: '© 2026 Champlain Pet Clinic. All rights reserved.',
            },
          },
        }),
    });

    // Act
    await loadActiveTranslations();

    // Assert
    const footer = await renderFooter();
    expect(footer.textContent).toContain('PetClinic Employee Portal');
    expect(footer.textContent).not.toContain('Portail Employé Clinique Vétérinaire');
  });
});

describe('flattenTranslations (VETS-CPC-2089)', () => {
  // POSITIVE - The three-level case, which is the one the templates actually need: the page uses
  // i18n="@@settings.security.title", and a dot in a message id is a character, never a path.
  it('rebuilds a dotted message id from the nesting', () => {
    // Arrange
    const grouped = {
      settings: {
        security: { title: 'Security' },
        display: { title: 'Display' },
      },
    };

    // Act
    const flat = flattenTranslations(grouped);

    // Assert
    expect(flat).toEqual({
      'settings.security.title': 'Security',
      'settings.display.title': 'Display',
    });
  });

  // POSITIVE - A flat catalogue comes back untouched. This idempotence is what lets the loader
  it('leaves an already flat catalogue unchanged', () => {
    // Arrange
    const flat = {
      'footer.brand': 'PetClinic Employee Portal',
      'login.title': 'Login',
    };

    // Act & Assert
    expect(flattenTranslations(flat)).toEqual(flat);
  });

  // POSITIVE - Real groups mix depths: settings.title is a leaf sitting next to settings.security,
  it('handles leaves and groups side by side in one group', () => {
    // Arrange
    const grouped = {
      settings: {
        title: 'Settings',
        save: 'Save',
        security: { title: 'Security' },
      },
    };

    // Act
    const flat = flattenTranslations(grouped);

    // Assert
    expect(flat).toEqual({
      'settings.title': 'Settings',
      'settings.save': 'Save',
      'settings.security.title': 'Security',
    });
  });
});
