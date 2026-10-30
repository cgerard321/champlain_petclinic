import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { Component, Input } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideRouter, Router, RouterLink } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { VetResponse } from '@features/vets/models/vet-api.model';
import { VetProfile } from '@features/vets/models/vet-profile.model';
import { VetList } from '@features/vets/pages/vet-list/vet-list';
import { Veterinarians } from '@features/vets/pages/veterinarians/veterinarians';
import routes from '@features/vets/routes';
import { VetService } from '@features/vets/services/vet.service';

/**
 * Test-only replacement for VetProfileCard.
 *
 * Direct Vitest execution does not automatically compile Angular
 * signal-input metadata, so the original input.required<VetProfile>()
 * is not recognized during these routing tests.
 *
 * The real VetProfileCard is tested separately in its own spec file.
 */
@Component({
  selector: 'app-vet-profile-card',
  standalone: true,
  template: `
    <article class="profile-card">
      <h2>{{ vet.firstName }} {{ vet.lastName }}</h2>
    </article>
  `,
})
class MockVetProfileCard {
  // Test-only compatibility: preserve the decorator-based input for Vitest routing tests.
  // eslint-disable-next-line @angular-eslint/prefer-signals
  @Input({ required: true }) vet!: VetProfile;
}

describe('Veterinarian directory navigation', () => {
  /**
   * Load the original HTML templates instead of replacing
   * them with simplified test templates.
   */
  const loadTemplate = (path: string): string => readFileSync(resolve(process.cwd(), path), 'utf8');

  beforeEach(async () => {
    vi.clearAllMocks();

    const veterinariansTemplate = loadTemplate(
      'src/app/features/vets/pages/veterinarians/veterinarians.html',
    );

    const vetListTemplate = loadTemplate('src/app/features/vets/pages/vet-list/vet-list.html');

    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          {
            path: 'vets',
            children: routes,
          },
        ]),

        {
          provide: VetService,
          useValue: {
            getVets: () =>
              of(
                ['Alex Morin', 'Julie Lambert', 'Sam Tremblay'].map((name, index): VetResponse => ({
                  vetId: `test-vet-${index}`,
                  firstName: name.split(' ')[0] ?? '',
                  lastName: name.split(' ')[1] ?? '',
                  vetBillId: null,
                  email: null,
                  phoneNumber: null,
                  resume: null,
                  workday: [],
                  workHoursJson: null,
                  active: true,
                  specialties: [],
                })),
              ),

            // Required by VetList after education integration.
            getEducations: () => of([]),
          },
        },

        {
          provide: AuthState,
          useValue: {
            hasRole: vi.fn().mockReturnValue(false),
          },
        },

        {
          provide: MatDialog,
          useValue: {
            open: vi.fn(),
          },
        },
      ],
    })

      /**
       * Use the real Veterinarians landing-page HTML.
       */
      .overrideComponent(Veterinarians, {
        set: {
          templateUrl: undefined,
          styleUrl: undefined,
          template: veterinariansTemplate,
          styles: [],
        },
      })

      /**
       * Use the real VetList HTML, but replace its
       * VetProfileCard dependency with a test-only component.
       */
      .overrideComponent(VetList, {
        set: {
          imports: [RouterLink, MockVetProfileCard],
          templateUrl: undefined,
          styleUrl: undefined,
          template: vetListTemplate,
          styles: [],
        },
      })

      .compileComponents();
  });

  it('opens the directory from the landing page and returns to the landing page', async () => {
    const harness = await RouterTestingHarness.create('/vets');

    const directoryLink =
      harness.routeNativeElement?.querySelector<HTMLAnchorElement>('a[href="/vets/list"]');

    expect(directoryLink).toBeTruthy();

    directoryLink?.click();

    await harness.fixture.whenStable();
    harness.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/vets/list');

    expect(harness.routeNativeElement?.querySelectorAll('app-vet-profile-card').length).toBe(3);

    expect(harness.routeNativeElement?.querySelectorAll('.profile-link').length).toBe(3);

    harness.routeNativeElement?.querySelector<HTMLAnchorElement>('a[href="/vets"]')?.click();

    await harness.fixture.whenStable();
    harness.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/vets');

    expect(harness.routeNativeElement?.querySelector('h1')?.textContent).toBe('Vétérinaires');
  });

  it('renders multiple distinct profiles when the directory is opened directly', async () => {
    const harness = await RouterTestingHarness.create('/vets/list');

    harness.detectChanges();

    const headings = harness.routeNativeElement?.querySelectorAll('.profile-card h2');

    expect(Array.from(headings ?? [], (heading) => heading.textContent?.trim())).toEqual([
      'Alex Morin',
      'Julie Lambert',
      'Sam Tremblay',
    ]);

    expect(harness.routeNativeElement?.textContent).not.toContain('Profils fictifs');
  });
});
