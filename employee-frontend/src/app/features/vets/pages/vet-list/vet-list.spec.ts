import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { VetService } from '@features/vets/services/vet.service';

import { VetList } from './vet-list';

describe('VetList - CPC-2195 Remove VETS', () => {
  let fixture: ComponentFixture<VetList>;

  let isAdmin: boolean;
  let dialogResult: Subject<boolean | undefined>;

  const vet = {
    vetId: 'vet-123',
    firstName: 'John',
    lastName: 'Smith',
    active: true,
    specialties: [
      {
        name: 'Surgery',
      },
    ],
    workday: ['Monday', 'Tuesday', 'Wednesday'],
    workHoursJson: '{}',
    email: 'john.smith@example.com',
    phoneNumber: '5145551234',
    resume: 'Experienced veterinarian specializing in surgery.',
  };

  const vetServiceMock = {
    getVets: vi.fn(),
    getEducations: vi.fn(),
    deleteVet: vi.fn(),
  };

  const authMock = {
    hasRole: vi.fn(),
  };

  const dialogMock = {
    open: vi.fn(),
  };

  const removeButtons = (): HTMLButtonElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll('.remove-vet-button'));

  const createComponent = (): ComponentFixture<VetList> => {
    fixture = TestBed.createComponent(VetList);
    fixture.detectChanges();
    return fixture;
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    isAdmin = true;
    dialogResult = new Subject<boolean | undefined>();

    authMock.hasRole.mockImplementation((role: string) => role === 'ADMIN' && isAdmin);

    vetServiceMock.getVets.mockReturnValue(of([vet]));
    vetServiceMock.getEducations.mockReturnValue(of([]));
    vetServiceMock.deleteVet.mockReturnValue(of(vet));

    dialogMock.open.mockReturnValue({
      afterClosed: () => dialogResult.asObservable(),
    });

    await TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: VetService,
          useValue: vetServiceMock,
        },
        {
          provide: AuthState,
          useValue: authMock,
        },
        {
          provide: MatDialog,
          useValue: dialogMock,
        },
      ],
    })
      .overrideComponent(VetList, {
        set: {
          imports: [],
          templateUrl: undefined,
          styleUrl: undefined,
          styles: [],
          template: `
            @for (vet of filteredVets(); track vet.vetId) {
              <div class="vet-item">
                <span>
                  {{ vet.firstName }} {{ vet.lastName }}
                </span>

                @if (isAdmin()) {
                  <button
                    type="button"
                    class="remove-vet-button"
                    [disabled]="deletingVetId() !== null"
                    (click)="removeVet(vet)"
                  >
                    Supprimer
                  </button>
                }
              </div>
            }

            @if (deleteError()) {
              <p role="alert">
                {{ deleteError() }}
              </p>
            }

            @if (deleteSuccess()) {
              <p role="status">
                {{ deleteSuccess() }}
              </p>
            }
          `,
        },
      })
      .compileComponents();
  });

  it('shows the Remove button to Admin', () => {
    createComponent();

    expect(removeButtons()).toHaveLength(1);
  });

  it('does not show the Remove button to non-Admin users', () => {
    isAdmin = false;

    createComponent();

    expect(removeButtons()).toHaveLength(0);
  });

  it('opens a confirmation dialog when Admin clicks Remove', () => {
    createComponent();

    removeButtons()[0]!.click();

    expect(dialogMock.open).toHaveBeenCalledTimes(1);
    expect(vetServiceMock.deleteVet).not.toHaveBeenCalled();

    const dialogCall = dialogMock.open.mock.calls[0];

    expect(dialogCall).toBeDefined();

    if (!dialogCall) {
      throw new Error('Confirmation dialog was not opened');
    }

    const dialogConfig = dialogCall[1] as {
      data: {
        message: string;
      };
    };

    expect(dialogCall[0]).toBeDefined();
    expect(dialogConfig.data.message).toContain('John Smith');
  });

  it('does not delete the veterinarian when confirmation is cancelled', () => {
    createComponent();

    removeButtons()[0]!.click();

    dialogResult.next(false);
    dialogResult.complete();

    fixture.detectChanges();

    expect(vetServiceMock.deleteVet).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('John Smith');
    expect(removeButtons()).toHaveLength(1);
  });

  it('deletes the veterinarian and updates the list after confirmation', () => {
    createComponent();

    removeButtons()[0]!.click();

    expect(vetServiceMock.deleteVet).not.toHaveBeenCalled();

    dialogResult.next(true);
    dialogResult.complete();

    expect(vetServiceMock.deleteVet).toHaveBeenCalledTimes(1);
    expect(vetServiceMock.deleteVet).toHaveBeenCalledWith('vet-123');

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('John Smith');
    expect(removeButtons()).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('[role="status"]')).not.toBeNull();
  });

  it('shows an error and preserves the veterinarian when DELETE fails', () => {
    vetServiceMock.deleteVet.mockReturnValue(throwError(() => new Error('Delete failed')));

    createComponent();

    removeButtons()[0]!.click();

    dialogResult.next(true);
    dialogResult.complete();

    fixture.detectChanges();

    expect(vetServiceMock.deleteVet).toHaveBeenCalledWith('vet-123');
    expect(fixture.nativeElement.textContent).toContain('John Smith');
    expect(removeButtons()).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
  });

  it('disables Remove while a DELETE request is pending', () => {
    const pendingDelete = new Subject<unknown>();

    vetServiceMock.deleteVet.mockReturnValue(pendingDelete.asObservable());

    createComponent();

    removeButtons()[0]!.click();

    dialogResult.next(true);

    fixture.detectChanges();

    expect(vetServiceMock.deleteVet).toHaveBeenCalledTimes(1);
    expect(removeButtons()).toHaveLength(1);
    expect(removeButtons()[0]!.disabled).toBe(true);

    pendingDelete.next(vet);
    pendingDelete.complete();

    fixture.detectChanges();

    expect(removeButtons()).toHaveLength(0);
    expect(fixture.nativeElement.textContent).not.toContain('John Smith');
  });
});
