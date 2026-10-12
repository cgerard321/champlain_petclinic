import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

import { FollowUpVisitDialog } from './FollowUpVisitDialog';

describe('FollowUpVisitDialog', () => {
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

  const createdVisit = {
    ...completedVisit,
    visitId: 'VIST-2701-1201',
    status: Status.UPCOMING,
  };

  const getAvailableDates = vi.fn<VisitService['getAvailableDates']>();
  const getAvailableTimeSlots = vi.fn<VisitService['getAvailableTimeSlots']>();
  const addFollowUpVisit = vi.fn<VisitService['addFollowUpVisit']>();
  const close = vi.fn();

  beforeEach(async () => {
    vi.clearAllMocks();
    getAvailableDates.mockReturnValue(of(['2027-01-12']));
    getAvailableTimeSlots.mockReturnValue(of([]));
    addFollowUpVisit.mockReturnValue(of(createdVisit));

    await TestBed.configureTestingModule({
      imports: [FollowUpVisitDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { visit: completedVisit } },
        { provide: MatDialogRef, useValue: { close } },
        {
          provide: VisitService,
          useValue: { getAvailableDates, getAvailableTimeSlots, addFollowUpVisit },
        },
      ],
    }).compileComponents();
  });

  it('shows the pet and veterinarian from the completed visit', () => {
    const fixture = TestBed.createComponent(FollowUpVisitDialog);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Leo');
    expect(fixture.nativeElement.textContent).toContain('Linda Douglas');
  });

  it('only allows days that have available times', () => {
    const component = TestBed.createComponent(FollowUpVisitDialog).componentInstance;

    expect(getAvailableDates).toHaveBeenCalledWith('vet-1', expect.any(String), expect.any(String));
    expect(component['isAvailableDay'](new Date(2027, 0, 12))).toBe(true);
    expect(component['isAvailableDay'](new Date(2027, 0, 13))).toBe(false);
  });

  it('keeps every day selectable when the available dates cannot be loaded', () => {
    getAvailableDates.mockReturnValue(throwError(() => new Error('Backend unavailable')));
    const component = TestBed.createComponent(FollowUpVisitDialog).componentInstance;

    expect(component['isAvailableDay'](new Date(2027, 0, 13))).toBe(true);
  });

  it('loads only future available time slots for the selected date', () => {
    getAvailableTimeSlots.mockReturnValue(
      of([
        {
          startTime: '2027-01-12T09:00:00',
          endTime: '2027-01-12T10:00:00',
          available: true,
        },
        {
          startTime: '2027-01-12T10:00:00',
          endTime: '2027-01-12T11:00:00',
          available: false,
        },
      ]),
    );
    const fixture = TestBed.createComponent(FollowUpVisitDialog);
    const component = fixture.componentInstance;

    component['loadTimeSlots'](new Date(2027, 0, 12));

    expect(getAvailableTimeSlots).toHaveBeenCalledWith('vet-1', '2027-01-12');
    expect(component['timeSlots']()).toEqual([
      {
        startTime: '2027-01-12T09:00:00',
        endTime: '2027-01-12T10:00:00',
        available: true,
      },
    ]);
  });

  it('does not submit when the description only contains spaces', () => {
    const component = TestBed.createComponent(FollowUpVisitDialog).componentInstance;
    component['model'].set({
      date: new Date(2027, 0, 12),
      visitDate: '2027-01-12T09:00:00',
      description: '   ',
    });

    component['submit'](new Event('submit', { cancelable: true }));

    expect(addFollowUpVisit).not.toHaveBeenCalled();
  });

  it('trims the description and closes with the scheduled visit', () => {
    const component = TestBed.createComponent(FollowUpVisitDialog).componentInstance;
    component['model'].set({
      date: new Date(2027, 0, 12),
      visitDate: '2027-01-12T09:00:00',
      description: '  Follow-up check  ',
    });

    component['submit'](new Event('submit', { cancelable: true }));

    expect(addFollowUpVisit).toHaveBeenCalledWith(
      completedVisit,
      '2027-01-12T09:00:00',
      'Follow-up check',
    );
    expect(close).toHaveBeenCalledWith(createdVisit);
  });

  it('shows an error when available times cannot be loaded', () => {
    getAvailableTimeSlots.mockReturnValue(throwError(() => new Error('Backend unavailable')));
    const component = TestBed.createComponent(FollowUpVisitDialog).componentInstance;

    component['loadTimeSlots'](new Date(2027, 0, 12));

    expect(component['isLoadingSlots']()).toBe(false);
    expect(component['errorMessage']()).toBe('Impossible de charger les heures disponibles.');
  });
});
