import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { Status } from '@features/vist/models/Status';
import { VisitResponseModel } from '@features/vist/models/VisitResponseModel';
import { VisitService } from '@features/vist/services/visit-service';

import { VisitDetails } from './visit-details';

describe('VisitDetails', () => {
  const visit: VisitResponseModel = {
    visitId: 'VIST-2303-1701',
    visitDate: '2023-03-17T16:00:00',
    visitEndDate: '2023-03-17T16:30:00',
    description: 'Dog Massage',
    petId: 'pet-1',
    petName: 'Leo',
    petBirthDate: '2020-01-01',
    practitionerId: 'vet-1',
    vetFirstName: 'Linda',
    vetLastName: 'Douglas',
    vetEmail: 'linda@example.com',
    vetPhoneNumber: '555-1234',
    status: Status.COMPLETED,
    isEmergency: false,
    ownerFirstName: 'John',
    ownerLastName: 'Smith',
  };

  const getVisitById = vi.fn();

  async function render(visitId = 'VIST-2303-1701'): Promise<ComponentFixture<VisitDetails>> {
    getVisitById.mockReturnValue(of(visit));

    await TestBed.configureTestingModule({
      imports: [VisitDetails],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'visitId' ? visitId : null),
              },
            },
          },
        },
        {
          provide: VisitService,
          useValue: { getVisitById },
        },
      ],
    }).compileComponents();

    const fixture: ComponentFixture<VisitDetails> = TestBed.createComponent(VisitDetails);
    fixture.detectChanges();

    return fixture;
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads and displays visit details', async () => {
    const fixture = await render();

    expect(getVisitById).toHaveBeenCalledWith('VIST-2303-1701');
    expect(fixture.nativeElement.textContent).toContain('Dog Massage');
    expect(fixture.nativeElement.textContent).toContain('Leo');
    expect(fixture.nativeElement.textContent).toContain('Linda');
  });

  it('shows an error when no visit ID is provided', async () => {
    const fixture = await render('');

    expect(getVisitById).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('No visit ID was provided.');
  });
});
