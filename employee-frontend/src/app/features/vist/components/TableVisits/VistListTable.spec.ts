import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';
import { Roles } from '@shared/models/roles';

import { VistListTable } from './VistListTable';

describe('VistListTable', () => {
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

  const upcomingVisit: Visit = {
    ...completedVisit,
    visitId: 'VIST-2701-1201',
    visitDate: new Date('2027-01-12T09:00:00'),
    visitEndDate: new Date('2027-01-12T10:00:00'),
    status: Status.UPCOMING,
  };

  const dialogOpen = vi.fn();
  const snackBarOpen = vi.fn();

  async function render(
    role: string,
    dialogResult?: Visit,
  ): Promise<ComponentFixture<VistListTable>> {
    dialogOpen.mockReturnValue({ afterClosed: () => of(dialogResult) });

    await TestBed.configureTestingModule({
      imports: [VistListTable],
      providers: [
        { provide: VisitService, useValue: { getAllVisits: () => of(completedVisit) } },
        { provide: AuthState, useValue: { hasRole: (value: string) => value === role } },
      ],
    })
      .overrideProvider(MatDialog, { useValue: { open: dialogOpen } })
      .overrideProvider(MatSnackBar, { useValue: { open: snackBarOpen } })
      .compileComponents();

    const fixture = TestBed.createComponent(VistListTable);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => vi.clearAllMocks());

  it('shows the follow-up action for a veterinarian on completed visits', async () => {
    const fixture = await render(Roles.vet);
    const button = fixture.nativeElement.querySelector('td button') as HTMLButtonElement;

    expect(button).not.toBeNull();
    expect(button.textContent).toContain('Planifier un suivi');
  });

  it('does not show the follow-up action to other employee roles', async () => {
    const fixture = await render(Roles.admin);

    expect(fixture.nativeElement.querySelector('td button')).toBeNull();
  });

  it('adds the scheduled follow-up to the table and shows confirmation', async () => {
    const fixture = await render(Roles.vet, upcomingVisit);
    const component = fixture.componentInstance;

    component['openFollowUpVisit'](completedVisit);

    expect(dialogOpen).toHaveBeenCalled();
    expect(component['visit']()).toEqual([upcomingVisit, completedVisit]);
    expect(snackBarOpen).toHaveBeenCalledWith('La visite de suivi a été planifiée.', '', {
      duration: 3000,
      verticalPosition: 'top',
    });
  });

  it('shows the add note action for a veterinarian on completed visits', async () => {
    const fixture = await render(Roles.vet);
    const buttons = fixture.nativeElement.querySelectorAll('td button');

    expect(buttons[1].textContent).toContain('Ajouter une note');
  });

  it('updates the visit with the saved note and shows confirmation', async () => {
    const visitWithNote = { ...completedVisit, vetNote: 'Keep giving the meds' };
    const fixture = await render(Roles.vet, visitWithNote);
    const component = fixture.componentInstance;

    component['openVetNote'](completedVisit);

    expect(dialogOpen).toHaveBeenCalled();
    expect(component['visit']()).toEqual([visitWithNote]);
    expect(snackBarOpen).toHaveBeenCalledWith('La note a été enregistrée.', '', {
      duration: 3000,
      verticalPosition: 'top',
    });
  });
});
