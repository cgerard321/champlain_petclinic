import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import { Bill, BillRequestModel } from '@features/bill/models/bill.model';
import { Customer } from '@features/bill/models/customer.model';
import { Veterinarian } from '@features/bill/models/vet.model';

@Injectable({ providedIn: 'root' })
export class BillService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);
  private readonly baseUrl = '/api/gateway/bills';

  createBill(body: BillRequestModel): Observable<Bill> {
    return this.http.post<Bill>(this.baseUrl, body);
  }

  getVetByVetId(vetId: string): Observable<Veterinarian> {
    return this.http.get<Veterinarian>(`/api/v2/gateway/vets/${vetId}`);
  }

  getCustomers(): Observable<Customer> {
    return this.sse.stream('/api/gateway/customers', { keepAlive: false }).pipe(
      filter((event): event is MessageEvent<string> => event.type !== 'error'),
      map((event) => JSON.parse(event.data) as Customer),
    );
  }
}
