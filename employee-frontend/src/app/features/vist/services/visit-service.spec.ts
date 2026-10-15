import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';

import { VisitService } from './visit-service';

describe('VisitService', () => {
  let service: VisitService;
  let http: HttpTestingController;

  const completedVisit: Visit = {
    visitId: 'VIST-2303-1701',
    visitDate: new Date('2023-03-17T16:00:00'),
    visitEndDate: new Date('2023-03-17T17:00:00'),
    description: 'Dog Massage',
    petId: 'pet-1',
    petName: 'Leo',
    petBirthDate: new Date('2020-01-01'),
    vetFirstName: 'Linda',
    vetLastName: 'Douglas',
    vetEmail: 'linda@example.com',
    vetPhoneNumber: '555-1234',
    practitionerId: 'vet-1',
    status: Status.COMPLETED,
    isEmergency: false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        VisitService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthState, useValue: { hasRole: () => false } },
        { provide: SseClient, useValue: {} },
      ],
    });
    service = TestBed.inject(VisitService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('gets the available time slots for the selected veterinarian and date', () => {
    service.getAvailableTimeSlots('vet-1', '2027-01-12').subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/gateway/visits/availability/vets/vet-1/slots' &&
        candidate.params.get('date') === '2027-01-12',
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('gets the available dates for the veterinarian within the date range', () => {
    service.getAvailableDates('vet-1', '2027-01-01', '2027-06-30').subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/gateway/visits/availability/vets/vet-1/dates' &&
        candidate.params.get('startDate') === '2027-01-01' &&
        candidate.params.get('endDate') === '2027-06-30',
    );
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates an upcoming follow-up for the same pet and veterinarian', () => {
    service.addFollowUpVisit(completedVisit, '2027-01-12T09:00:00', 'Follow-up check').subscribe();

    const request = http.expectOne('/api/gateway/visits');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      visitDate: '2027-01-12T09:00:00',
      description: 'Follow-up check',
      petId: completedVisit.petId,
      practitionerId: completedVisit.practitionerId,
      status: Status.UPCOMING,
      isEmergency: false,
    });
    request.flush({ ...completedVisit, status: Status.UPCOMING });
  });

  it('saves a note on the visit', () => {
    service.addVetNote(completedVisit.visitId, 'Keep giving the meds').subscribe();

    const request = http.expectOne(`/api/gateway/visits/${completedVisit.visitId}/note`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ vetNote: 'Keep giving the meds' });
    request.flush({ ...completedVisit, vetNote: 'Keep giving the meds' });
  });
});
