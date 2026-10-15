import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { FollowUpVisitDialog } from '@features/vist/components/FollowUpVisit/FollowUpVisitDialog';
import { NextAppointment } from '@features/vist/components/NextAppointment/NextAppointment';
import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

describe('NextAppointment', () => {
  let fixture: ComponentFixture<NextAppointment>;
  let visits: Subject<Visit>;
  let isVet: boolean;
  let afterClosed: Subject<Visit | undefined>;
  let dialog: { open: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  const makeVisit = (overrides: Partial<Visit> = {}): Visit => ({
    visitId: 'visit-1',
    visitDate: new Date(Date.now() + 60 * 60 * 1000),
    visitEndDate: new Date(Date.now() + 2 * 60 * 60 * 1000),
    description: 'Routine checkup',
    petId: 'pet-1',
    petName: 'Leo',
    petBirthDate: new Date('2020-01-01T00:00:00'),
    vetFirstName: 'Linda',
    vetLastName: 'Douglas',
    vetEmail: 'linda@example.com',
    vetPhoneNumber: '555-1234',
    practitionerId: 'vet-1',
    status: Status.UPCOMING,
    isEmergency: false,
    ...overrides,
  });

  beforeEach(async () => {
    visits = new Subject<Visit>();
    afterClosed = new Subject<Visit | undefined>();
    isVet = false;
    dialog = {
      open: vi.fn(() => ({ afterClosed: () => afterClosed.asObservable() })),
    };
    snackBar = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [NextAppointment],
      providers: [
        { provide: VisitService, useValue: { getAllVisits: () => visits.asObservable() } },
        { provide: AuthState, useValue: { hasRole: (role: string) => role === 'VET' && isVet } },
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();
  });

  function createFixture(): void {
    fixture = TestBed.createComponent(NextAppointment);
    fixture.detectChanges();
  }

  it('shows the closest upcoming visit and adds a scheduled follow-up when the dialog closes with one', () => {
    isVet = true;
    createFixture();
    const laterVisit = makeVisit({
      visitId: 'visit-later',
      visitDate: new Date(Date.now() + 3 * 60 * 60 * 1000),
    });
    const closestVisit = makeVisit({
      visitId: 'visit-closest',
      status: Status.COMPLETED,
      visitDate: new Date(Date.now() + 60 * 60 * 1000),
    });
    visits.next(laterVisit);
    visits.next(closestVisit);
    visits.complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('visit-closest');
    expect(fixture.nativeElement.textContent).not.toContain('visit-later');
    expect(fixture.nativeElement.textContent).toContain('Linda Douglas');

    fixture.nativeElement.querySelector('button').click();
    expect(dialog.open).toHaveBeenCalledWith(FollowUpVisitDialog, {
      data: { visit: closestVisit },
      width: '620px',
      maxWidth: '95vw',
    });

    const followUp = makeVisit({
      visitId: 'visit-follow-up',
      description: 'Follow-up check',
      visitDate: new Date(Date.now() + 30 * 60 * 1000),
    });
    afterClosed.next(followUp);
    afterClosed.complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('visit-follow-up');
    expect(snackBar.open).toHaveBeenCalledWith(
      'La visite de suivi a été planifiée.',
      '',
      { duration: 3000, verticalPosition: 'top' },
    );
  });

  it('hides past visits and follow-up actions for users who are not veterinarians', () => {
    isVet = false;
    createFixture();
    const pastVisit = makeVisit({
      visitId: 'visit-past',
      visitDate: new Date(Date.now() - 60 * 60 * 1000),
    });
    const soonest = makeVisit({
      visitId: 'visit-soonest',
      visitDate: new Date(Date.now() + 30 * 60 * 1000),
    });
    const later = makeVisit({
      visitId: 'visit-later',
      visitDate: new Date(Date.now() + 90 * 60 * 1000),
    });
    visits.next(pastVisit);
    visits.next(later);
    visits.next(soonest);
    visits.complete();
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('visit-soonest');
    expect(content).not.toContain('visit-past');
    expect(content).not.toContain('visit-later');
    expect(fixture.nativeElement.querySelector('button')).toBeNull();
  });

  it('keeps the visit list unchanged when the follow-up dialog is dismissed', () => {
    isVet = true;
    createFixture();
    const completedVisit = makeVisit({ status: Status.COMPLETED });
    visits.next(completedVisit);
    visits.complete();
    fixture.detectChanges();

    fixture.nativeElement.querySelector('button').click();
    afterClosed.next(undefined);
    afterClosed.complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('visit-1');
    expect(snackBar.open).not.toHaveBeenCalled();
  });

  it('stops loading and records the load error when the visit stream fails', () => {
    createFixture();
    visits.error(new Error('visit stream unavailable'));

    const state = fixture.componentInstance as unknown as {
      isLoading: () => boolean;
      errorMessage: () => string | null;
      visibleVisits: () => Visit[];
    };
    expect(state.isLoading()).toBe(false);
    expect(state.errorMessage()).toBe('Unable to load visits. Please try again later');
    expect(state.visibleVisits()).toEqual([]);
  });
});



