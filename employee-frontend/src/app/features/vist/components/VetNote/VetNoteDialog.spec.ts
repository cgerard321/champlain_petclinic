import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

import { VetNoteDialog } from './VetNoteDialog';

describe('VetNoteDialog', () => {
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

  const addVetNote = vi.fn<VisitService['addVetNote']>();
  const close = vi.fn();

  beforeEach(async () => {
    vi.clearAllMocks();
    addVetNote.mockReturnValue(of({ ...completedVisit, vetNote: 'Keep giving the meds' }));

    await TestBed.configureTestingModule({
      imports: [VetNoteDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { visit: completedVisit } },
        { provide: MatDialogRef, useValue: { close } },
        { provide: VisitService, useValue: { addVetNote } },
      ],
    }).compileComponents();
  });

  it('shows the pet and veterinarian from the completed visit', () => {
    const fixture = TestBed.createComponent(VetNoteDialog);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Leo');
    expect(fixture.nativeElement.textContent).toContain('Linda Douglas');
  });

  it('does not save a blank note', () => {
    const component = TestBed.createComponent(VetNoteDialog).componentInstance;
    component['model'].set({ vetNote: '   ' });

    component['submit'](new Event('submit'));

    expect(addVetNote).not.toHaveBeenCalled();
    expect(close).not.toHaveBeenCalled();
  });

  it('does not save a note shorter than 5 characters', () => {
    const component = TestBed.createComponent(VetNoteDialog).componentInstance;
    component['model'].set({ vetNote: '  ok  ' });

    component['submit'](new Event('submit'));

    expect(addVetNote).not.toHaveBeenCalled();
  });

  it('saves the trimmed note and closes with the updated visit', () => {
    const component = TestBed.createComponent(VetNoteDialog).componentInstance;
    component['model'].set({ vetNote: '  Keep giving the meds  ' });

    component['submit'](new Event('submit'));

    expect(addVetNote).toHaveBeenCalledWith('VIST-2303-1701', 'Keep giving the meds');
    expect(close).toHaveBeenCalledWith({ ...completedVisit, vetNote: 'Keep giving the meds' });
  });

  it('shows the error message when the note cannot be saved', () => {
    addVetNote.mockReturnValue(
      throwError(() => ({
        code: 400,
        message: 'Cannot add a note to a visit that is not completed.',
      })),
    );
    const component = TestBed.createComponent(VetNoteDialog).componentInstance;
    component['model'].set({ vetNote: 'Keep giving the meds' });

    component['submit'](new Event('submit'));

    expect(component['errorMessage']()).toBe('Cannot add a note to a visit that is not completed.');
    expect(close).not.toHaveBeenCalled();
  });
});
