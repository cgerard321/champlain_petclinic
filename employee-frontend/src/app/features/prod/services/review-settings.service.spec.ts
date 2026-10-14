import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ReviewSettingsService } from './review-settings.service';

describe('ReviewSettingsService', () => {
  let service: ReviewSettingsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReviewSettingsService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ReviewSettingsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('gets review settings', () => {
    service.getSettings().subscribe((settings) => {
      expect(settings.editPeriodHours).toBe(48);
    });

    const request = http.expectOne('/api/gateway/review-settings');

    expect(request.request.method).toBe('GET');
    request.flush({ editPeriodHours: 48 });
  });

  it('updates review settings', () => {
    service.updateSettings({ editPeriodHours: 72 }).subscribe((settings) => {
      expect(settings.editPeriodHours).toBe(72);
    });

    const request = http.expectOne('/api/gateway/review-settings');

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      editPeriodHours: 72,
    });

    request.flush({ editPeriodHours: 72 });
  });
});
