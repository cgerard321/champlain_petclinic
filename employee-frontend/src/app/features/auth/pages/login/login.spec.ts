import { ComponentFixture, TestBed } from '@angular/core/testing';
import { clearTranslations, loadTranslations } from '@angular/localize';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { Auth } from '@features/auth/services/auth';

import { LoginPage } from './login';

/**
 * VETS-CPC-1927 - login page internationalization.
 *
 * Same mechanism as the footer, but on a component that has injected dependencies, which
 * confirms the translations also apply inside an injection context. The login page is the only
 * translated page that sits outside the shell, so it is also the only one a user sees before
 * authenticating.
 */

/** The login subset of src/locale/en.json, kept in sync with that file. */
const EN_LOGIN = {
  'login.title': 'Login',
  'login.username': 'Username or email',
  'login.password': 'Password',
  'login.submit': 'Sign in',
};

describe('LoginPage', () => {
  const auth = {
    login: vi.fn(),
  };

  const router = {
    navigateByUrl: vi.fn(),
  };

  afterEach(() => {
    // Keeps the English loaded by one test from leaking into the next.
    clearTranslations();
  });

  async function renderLogin(): Promise<HTMLElement> {
    vi.clearAllMocks();
    auth.login.mockReturnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [
        { provide: Auth, useValue: auth },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    const fixture: ComponentFixture<LoginPage> = TestBed.createComponent(LoginPage);
    await fixture.whenStable();

    return fixture.nativeElement as HTMLElement;
  }

  it('renders the French source labels when no translations are loaded', async () => {
    // Act
    const page = await renderLogin();

    // Assert
    expect(page.textContent).toContain('Connexion');
    expect(page.textContent).toContain("Nom d'utilisateur ou courriel");
    expect(page.textContent).toContain('Mot de passe');
    expect(page.textContent).toContain('Se connecter');
  });

  it('renders the English labels once the translations are loaded', async () => {
    // Arrange - before rendering, so $localize picks the translations up
    loadTranslations(EN_LOGIN);

    // Act
    const page = await renderLogin();

    // Assert
    expect(page.textContent).toContain('Login');
    expect(page.textContent).toContain('Username or email');
    expect(page.textContent).toContain('Password');
    expect(page.textContent).toContain('Sign in');
    expect(page.textContent).not.toContain('Mot de passe');
  });
});
