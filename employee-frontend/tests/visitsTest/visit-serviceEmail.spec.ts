import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EMPTY } from 'rxjs';

import { AuthState } from '../../src/app/core/services/auth-state';
import { SseClient } from '../../src/app/core/services/sse-client';
import { VisitService } from '../../src/app/features/vist/services/visit-service';

describe('VisitService weekly schedule email', () => {
  let service: VisitService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        VisitService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthState, useValue: { hasRole: () => false } },
        { provide: SseClient, useValue: { stream: () => EMPTY } },
      ],
    });
    service = TestBed.inject(VisitService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('posts to weekly-email and reads the text response', () => {
    let result: string | undefined;

    service.sendWeeklyScheduleEmail().subscribe((message) => (result = message));

    const request = http.expectOne('/api/gateway/visits/weekly-email');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    expect(request.request.responseType).toBe('text');
    request.flush('Message queued');

    expect(result).toBe('Message queued');
  });
});
