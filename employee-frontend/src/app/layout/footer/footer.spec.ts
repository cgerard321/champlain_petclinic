//angular utils for tests
import { TestBed } from '@angular/core/testing';
//it charges like a dictionary from translations
import { clearTranslations, loadTranslations } from '@angular/localize';

import { Footer } from './footer';

/** it define each value of the footer into its english translation */
const EN_FOOTER = {
  'footer.brand': 'PetClinic Employee Portal',
  'footer.privacy': 'Privacy Policy',
  'footer.terms': 'Terms of Service',
  'footer.support': 'Support',
  'footer.copyright': '© 2026 Champlain Pet Clinic. All rights reserved.',
};

describe('Footer', () => {
  afterEach(() => {
    // Translations live in a global map. Without this, the English loaded by one test would
    // leak into the next one and make the suite order-dependent.
    clearTranslations();
  });

  async function renderFooter(): Promise<HTMLElement> {
    await TestBed.configureTestingModule({ imports: [Footer] }).compileComponents();
    const fixture = TestBed.createComponent(Footer);
    await fixture.whenStable();

    return fixture.nativeElement as HTMLElement;
  }

  it('should create', async () => {
    // Arrange
    await TestBed.configureTestingModule({ imports: [Footer] }).compileComponents();

    // Act
    const fixture = TestBed.createComponent(Footer);

    // Assert
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the French source text when no translations are loaded', async () => {
    // Act
    const footer = await renderFooter();

    // Assert - proves the French text comes from the template itself, not from a catalogue
    expect(footer.textContent).toContain('Portail Employé Clinique Vétérinaire');
    expect(footer.textContent).toContain('Politique de confidentialité');
    expect(footer.textContent).toContain("Conditions d'utilisation");
    expect(footer.textContent).toContain('Tous droits réservés');
  });

  it('renders the English text once the translations are loaded', async () => {
    // Arrange - must happen before the component renders, because $localize is only
    // substituted for calls evaluated after loadTranslations()
    loadTranslations(EN_FOOTER);

    // Act
    const footer = await renderFooter();

    // Assert
    expect(footer.textContent).toContain('PetClinic Employee Portal');
    expect(footer.textContent).toContain('Privacy Policy');
    expect(footer.textContent).toContain('Terms of Service');
    expect(footer.textContent).toContain('All rights reserved');
    expect(footer.textContent).not.toContain('Portail Employé Clinique Vétérinaire');
  });
});
