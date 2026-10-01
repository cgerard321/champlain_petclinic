import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';

import { Header } from './header';

describe('Header', () => {
  // Shared by every test; created again in beforeEach.
  let fixture: ComponentFixture<Header>;

  // Fake location.reload(): records calls instead of actually reloading the page.
  const reload = vi.fn();

  // Fake AuthState with only what the header uses, so no real HTTP call is made.
  const authState = {
    logout: vi.fn(),
  };

  /** Clicks the header button whose label matches, or fails loudly if it is missing. */
  function clickLangButton(label: string): void {
    const element = fixture.nativeElement as HTMLElement;
    const buttons = Array.from(element.querySelectorAll('button'));
    const button = buttons.find((candidate) => candidate.textContent?.trim() === label);

    // A clear error message instead of "cannot read properties of undefined".
    if (!button) {
      throw new Error(`No "${label}" button found in the header`);
    }

    button.click();
  }

  beforeEach(async () => {
    // Start every test from a clean state: no recorded calls, no saved language.
    vi.clearAllMocks();
    localStorage.clear();

    // The real logout() returns an Observable, so the fake one does too.
    authState.logout.mockReturnValue(of(undefined));

    // jsdom does not implement navigation, so location.reload() would throw. The real object is
    // spread so anything Angular's router reads from it still works.
    vi.stubGlobal('location', { ...window.location, reload });

    // Render the header with an empty router and the fake AuthState instead of the real one.
    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([]), { provide: AuthState, useValue: authState }],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
  });

  afterEach(() => {
    // Restore the real location object and remove the saved language.
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  // POSITIVE - The header is created and the existing navbar still renders next to the switcher.
  it('should create', () => {
    // Assert - also confirms the navbar from CPC-1971 still renders alongside the switcher
    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Champlain Petclinic');
  });

  // POSITIVE - Clicking EN from the default French saves 'en' and reloads the page once.
  it('stores the chosen language and reloads when switching to English', () => {
    // Act
    clickLangButton('EN');

    // Assert - persistence is what makes the choice survive the reload
    expect(localStorage.getItem('lang')).toBe('en');
    expect(reload).toHaveBeenCalledTimes(1);
  });

  // POSITIVE (edge case) - Clicking the language that is already active does not reload the page.
  it('does nothing when the chosen language is already active', () => {
    // Arrange
    localStorage.setItem('lang', 'fr');

    // Act
    clickLangButton('FR');

    // Assert - without this guard, clicking the active language would reload the page for
    // nothing, which the user would see as a pointless flash
    expect(reload).not.toHaveBeenCalled();
    expect(localStorage.getItem('lang')).toBe('fr');
  });

  // POSITIVE - Clicking FR while English is active saves 'fr' and reloads the page once.
  it('switches back to French after English was selected', () => {
    // Arrange
    localStorage.setItem('lang', 'en');

    // Act
    clickLangButton('FR');

    // Assert
    expect(localStorage.getItem('lang')).toBe('fr');
    expect(reload).toHaveBeenCalledTimes(1);
  });
});
