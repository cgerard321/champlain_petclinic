import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EMPTY, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '../../src/app/core/services/auth-state';
import { VisitService } from '../../src/app/features/vist/services/visit-service';

import { WeeklyCalendar } from '../../src/app/features/vist/pages/weekly-calendar/weekly-calendar';

describe('WeeklyCalendar weekly email action', () => {
  let fixture: ComponentFixture<WeeklyCalendar>;
  let visitService: {
    getAllVisits: ReturnType<typeof vi.fn>;
    getPetById: ReturnType<typeof vi.fn>;
    getCustomerById: ReturnType<typeof vi.fn>;
    sendWeeklyScheduleEmail: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    visitService = {
      getAllVisits: vi.fn(() => EMPTY),
      getPetById: vi.fn(),
      getCustomerById: vi.fn(),
      sendWeeklyScheduleEmail: vi.fn(() => EMPTY),
    };

    await TestBed.configureTestingModule({
      imports: [WeeklyCalendar],
      providers: [
        provideRouter([]),
        { provide: AuthState, useValue: { hasRole: vi.fn(() => false), userId: vi.fn(() => 'vet-1') } },
        { provide: VisitService, useValue: visitService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WeeklyCalendar);
    fixture.detectChanges();
  });

  function emailButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelectorAll('button')[3] as HTMLButtonElement;
  }

  it('shows a sending state and then displays the service response', () => {
    const response = new Subject<string>();
    visitService.sendWeeklyScheduleEmail.mockReturnValue(response.asObservable());
    const button = emailButton();

    button.click();
    fixture.detectChanges();

    expect(visitService.sendWeeklyScheduleEmail).toHaveBeenCalledOnce();
    expect(button.disabled).toBe(true);
    expect(button.textContent).toContain('Sending...');

    response.next('Message queued');
    response.complete();
    fixture.detectChanges();

    expect(button.disabled).toBe(false);
    expect(button.textContent).toContain('Email me this week’s schedule');
    expect(fixture.nativeElement.querySelector('[role="status"]')?.textContent.trim()).toBe('Message queued');
  });

  it('shows an error message and re-enables the button when sending fails', () => {
    visitService.sendWeeklyScheduleEmail.mockReturnValue(
      throwError(() => new Error('mailer unavailable')),
    );
    const button = emailButton();

    button.click();
    fixture.detectChanges();

    expect(button.disabled).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="status"]')?.textContent.trim()).toBe(
      'Could not send the schedule email.',
    );
  });
});
