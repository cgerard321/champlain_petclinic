import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { VetResponse } from '@features/vets/models/vet-api.model';
import routes from '@features/vets/routes';
import { VetService } from '@features/vets/services/vet.service';

describe('Veterinarian directory navigation', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthState, useValue: { hasRole: () => false } },
        provideRouter([{ path: 'vets', children: routes }]),
        {
          provide: VetService,
          useValue: {
            getEducations: () => of([]),
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
          },
        },
      ],
    });
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
    const headings = harness.routeNativeElement?.querySelectorAll('.profile-card h2');

    expect(Array.from(headings ?? [], (heading) => heading.textContent?.trim())).toEqual([
      'Alex Morin',
      'Julie Lambert',
      'Sam Tremblay',
    ]);
    expect(harness.routeNativeElement?.textContent).not.toContain('Profils fictifs');
  });
});
