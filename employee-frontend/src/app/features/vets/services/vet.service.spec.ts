import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { VetService } from '@features/vets/services/vet.service';

describe('Veterinarian endpoint responses', () => {
  let service: VetService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(VetService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  for (const status of [200, 201]) {
    it(`collects separate veterinarian events from a ${status} response`, async () => {
      const vets = [
        { vetId: 'first-vet', firstName: 'Alex', lastName: 'Morin' },
        { vetId: 'second-vet', firstName: 'Julie', lastName: 'Lambert' },
      ];
      const result = firstValueFrom(service.getVets());
      const request = http.expectOne('/api/v2/gateway/vets');
      expect(request.request.method).toBe('GET');
      expect(request.request.headers.get('Accept')).toBe('text/event-stream');
      request.flush(vets.map((vet) => `data:${JSON.stringify(vet)}\n\n`).join(''), {
        status,
        statusText: status === 200 ? 'OK' : 'Created',
        headers: { 'Content-Type': 'text/event-stream' },
      });
      expect(await result).toEqual(vets);
    });
  }

  it('returns an empty list for a successful empty stream', async () => {
    const result = firstValueFrom(service.getVets());
    http.expectOne('/api/v2/gateway/vets').flush('', {
      headers: { 'Content-Type': 'text/event-stream' },
    });
    expect(await result).toEqual([]);
  });

  it('propagates a failed request instead of reporting an empty list', async () => {
    const result = firstValueFrom(service.getVets());
    const rejection = expect(result).rejects.toMatchObject({ status: 503 });
    http.expectOne('/api/v2/gateway/vets').flush('Unavailable', {
      status: 503,
      statusText: 'Service Unavailable',
    });
    await rejection;
  });
});
