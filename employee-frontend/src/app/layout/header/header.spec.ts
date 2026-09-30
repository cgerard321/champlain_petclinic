import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';

import { Header } from './header';

describe('Header', () => {
  let fixture: ComponentFixture<Header>;

  const reload = vi.fn();

  const authState = {
    logout: vi.fn(),
  };

  /** Clicks the header button whose label matches, or fails loudly if it is missing. */
  function clickLangButton(label: string): void {
    const element = fixture.nativeElement as HTMLElement;
    const buttons = Array.from(element.querySelectorAll('button'));
    const button = buttons.find((candidate) => candidate.textContent?.trim() === label);

    if (!button) {
      throw new Error(`No "${label}" button found in the header`);
    }

    button.click();
  }

  beforeEach(async () => {
    vi.clearAllMocks();
    localStorage.clear();

    authState.logout.mockReturnValue(of(undefined));

    // jsdom does not implement navigation, so location.reload() would throw. The real object is
    // spread so anything Angular's router reads from it still works.
    vi.stubGlobal('location', { ...window.location, reload });

    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([]), { provide: AuthState, useValue: authState }],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  it('should create', () => {
    // Assert
    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Champlain Petclinic');
  });

  it('stores the chosen language and reloads when switching to English', () => {
    // Act
    clickLangButton('EN');

    // Assert - persistence is what makes the choice survive the reload
    expect(localStorage.getItem('lang')).toBe('en');
    expect(reload).toHaveBeenCalledTimes(1);
  });

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
