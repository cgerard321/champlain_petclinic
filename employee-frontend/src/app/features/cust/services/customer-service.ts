import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, filter, map } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import {
  Customer,
  CustomerRequest,
  Pet,
  PetRequest,
  PetType,
} from '@features/cust/models/customer.model';
import { CustomerRequestModel } from '@features/cust/models/customerRequestModel';
import { CustomerResponseModel } from '@features/cust/models/customerResponseModel';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);

  private readonly apiUrl = '/api/gateway';

  getCustomers(): Observable<Customer> {
    return this.sse.stream(`${this.apiUrl}/customers`, { keepAlive: false }).pipe(
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
      .stream(`${this.apiUrl}/pets/customers/${customerId}/pets`, {
        keepAlive: false,
      })
      .pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Pet),
      );
  }

  getPetTypes(): Observable<PetType> {
    return this.sse.stream(`${this.apiUrl}/customers/petTypes`, { keepAlive: false }).pipe(
      filter((event): event is MessageEvent<string> => event.type !== 'error'),
      map((event) => JSON.parse(event.data) as PetType),
    );
  }

  getAllPets(): Observable<Pet> {
    return this.sse.stream(`${this.apiUrl}/pets`, { keepAlive: false }).pipe(
      filter((event): event is MessageEvent<string> => event.type !== 'error'),
      map((event) => JSON.parse(event.data) as Pet),
    );
  }

  addCustomer(customer: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(`${this.apiUrl}/customers`, customer);
  }

  addPet(customerId: string, pet: PetRequest): Observable<Pet> {
    return this.http.post<Pet>(`${this.apiUrl}/pets/customers/${customerId}/pets`, pet);
  }

  softDeleteCustomer(customerId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/customers/${customerId}/soft`);
  }
}
