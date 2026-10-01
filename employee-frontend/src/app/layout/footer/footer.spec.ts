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

  // Helper that renders the footer and returns its HTML, so tests can read the displayed text.
  async function renderFooter(): Promise<HTMLElement> {
    // Set up a small Angular test environment containing only the Footer component.
    await TestBed.configureTestingModule({ imports: [Footer] }).compileComponents();

    // Create an instance of the component (the fixture gives access to its class and its HTML).
    const fixture = TestBed.createComponent(Footer);

    // Wait until the component has finished rendering before reading its content.
    await fixture.whenStable();

    // Return the rendered HTML element of the footer.
    return fixture.nativeElement as HTMLElement;
  }

  // POSITIVE - The Footer component can be created without errors.
  it('should create', async () => {
    // Arrange
    await TestBed.configureTestingModule({ imports: [Footer] }).compileComponents();

    // Act
    const fixture = TestBed.createComponent(Footer);

    // Assert - the component instance exists
    expect(fixture.componentInstance).toBeTruthy();
  });

  // POSITIVE - With no translations loaded, the footer displays the French text from its template.
  it('renders the French source text when no translations are loaded', async () => {
    // Act
    const footer = await renderFooter();

    // Assert - proves the French text comes from the template itself, not from a catalogue
    expect(footer.textContent).toContain('Portail Employé Clinique Vétérinaire');
    expect(footer.textContent).toContain('Politique de confidentialité');
    expect(footer.textContent).toContain("Conditions d'utilisation");
    expect(footer.textContent).toContain('Tous droits réservés');
  });

  // POSITIVE - Once the English translations are loaded, the footer displays English instead of French.
  it('renders the English text once the translations are loaded', async () => {
    // Arrange - must happen before the component renders, because $localize is only
    // substituted for calls evaluated after loadTranslations()
    loadTranslations(EN_FOOTER);

    // Act
    const footer = await renderFooter();

    // Assert - every footer text is now in English
    expect(footer.textContent).toContain('PetClinic Employee Portal');
    expect(footer.textContent).toContain('Privacy Policy');
    expect(footer.textContent).toContain('Terms of Service');
    expect(footer.textContent).toContain('All rights reserved');

    // Assert - the French text was replaced, not just displayed next to the English one
    expect(footer.textContent).not.toContain('Portail Employé Clinique Vétérinaire');
  });
});
