import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, filter, map } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import { Customer, Pet, PetType } from '@features/cust/models/customer.model';
import { CustomerRequestModel } from '@features/cust/models/customerRequestModel';
import { CustomerResponseModel } from '@features/cust/models/customerResponseModel';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);

  getCustomers(): Observable<Customer> {
    return this.sse.stream('/api/gateway/customers', { keepAlive: false }).pipe(
      filter((event): event is MessageEvent<string> => event.type !== 'error'),
      map((event) => JSON.parse(event.data) as Customer),
    );
  }
  getCustomer(customerId: string, includePhoto = false): Observable<CustomerResponseModel> {
    return this.http.get<CustomerResponseModel>(
      `/api/gateway/customers/detail/${encodeURIComponent(customerId)}`,
      { params: { includePhoto } },
    );
  }
  getPetWithPhoto(petId: string): Observable<Pet> {
    return this.http.get<Pet>(`/api/gateway/pets/${encodeURIComponent(petId)}`, {
      params: { includePhoto: true },
    });
  }
  updateCustomer(
    customerId: string,
    customer: CustomerRequestModel,
  ): Observable<CustomerResponseModel> {
    return this.http.put<CustomerResponseModel>(
      `/api/gateway/customers/${encodeURIComponent(customerId)}`,
      customer,
    );
  }
  getPetDetails(customerId: string): Observable<Pet> {
    return this.sse
      .stream(`/api/gateway/pets/customers/${customerId}/pets`, { keepAlive: false })
      .pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Pet),
      );
  }
  getPetTypes(): Observable<PetType> {
    return this.sse
      .stream('/api/gateway/customers/petTypes', {
        keepAlive: false,
      })
      .pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as PetType),
      );
  }
}
