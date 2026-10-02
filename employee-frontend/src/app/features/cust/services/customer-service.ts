import { Injectable, inject } from '@angular/core';
import { Observable, filter, map } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import { Customer } from '@features/cust/models/customer.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly sse = inject(SseClient);

  getCustomers(): Observable<Customer> {
    return this.sse.stream('/api/gateway/customers', { keepAlive: false }).pipe(
      filter((event): event is MessageEvent<string> => event.type !== 'error'),
      map((event) => JSON.parse(event.data) as Customer),
    );
  }
}
