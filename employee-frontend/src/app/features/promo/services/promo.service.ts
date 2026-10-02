import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { filter, map, toArray } from 'rxjs/operators';

import { SseClient } from '@core/services/sse-client';
import { Promo, PromoRequest } from '@features/promo/models/promo.model';

@Injectable({ providedIn: 'root' })
export class PromoService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v2/gateway/promos';

  getPromos(): Observable<Promo[]> {
    return this.sse.stream(this.baseUrl, { keepAlive: false }).pipe(
      filter((event): event is MessageEvent => event.type !== 'error'),
      map((event) => JSON.parse(event.data) as Promo),
      toArray(),
    );
  }

  createPromo(request: PromoRequest): Observable<Promo> {
    return this.http.post<Promo>(this.baseUrl, request);
  }

  updatePromo(promoId: string, request: PromoRequest): Observable<Promo> {
    return this.http.put<Promo>(`${this.baseUrl}/${promoId}`, request);
  }

  deletePromo(promoId: string): Observable<Promo> {
    return this.http.delete<Promo>(`${this.baseUrl}/${promoId}`);
  }
}
