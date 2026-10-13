import { formatDate } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { EMPTY, filter, map, Observable, switchMap } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import { BillRecord, BillRequestModel, BillUpdateRequest } from '@features/bill/models/bill.model';
import { Customer } from '@features/bill/models/customer.model';
import { Veterinarian } from '@features/bill/models/vet.model';
import { Roles } from '@shared/models/roles';

type BillApiRecord = Omit<BillRecord, 'date' | 'dueDate'> & {
  date: string;
  dueDate: string;
};

@Injectable({
  providedIn: 'root',
})
export class BillService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);
  private readonly auth = inject(AuthState);

  private readonly baseUrl = '/api/gateway/bills';

  // CREATE BILL - FROM MAIN

  createBill(body: BillRequestModel): Observable<BillRecord> {
    const request = {
      ...body,
      date: formatDate(body.date, 'yyyy-MM-dd', 'en-US'),
    };

    return this.http
      .post<BillApiRecord>(this.baseUrl, request)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }

  // GET VETERINARIAN - FROM MAIN

  getVetByVetId(vetId: string): Observable<Veterinarian> {
    return this.http.get<Veterinarian>(`/api/v2/gateway/vets/${encodeURIComponent(vetId)}`);
  }

  // GET CUSTOMERS - FROM MAIN

  getCustomers(): Observable<Customer> {
    return this.sse
      .stream('/api/gateway/customers', {
        keepAlive: false,
      })
      .pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Customer),
      );
  }

  // GET ALL BILLS - CPC-1827

  getAllBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.admin)) {
      return EMPTY;
    }

    return this.getBillsFromUrl(this.baseUrl);
  }

  // GET BILL BY ID - CPC-1827

  getBillById(billId: string): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.admin) && !this.auth.hasRole(Roles.vet)) {
      return EMPTY;
    }

    return this.http
      .get<BillApiRecord>(`${this.baseUrl}/${encodeURIComponent(billId)}`)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }

  // GET BILLS BY VET ID - CPC-1827

  getBillsByVetId(vetId: string): Observable<BillRecord> {
    return this.getBillsFromUrl(`${this.baseUrl}/vets/${encodeURIComponent(vetId)}`);
  }

  // GET LOGGED-IN VET'S BILLS - CPC-1827

  getMyBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.vet)) {
      return EMPTY;
    }

    return this.http
      .get<CurrentUserResponse>('/api/gateway/users/jwt')
      .pipe(switchMap((user) => this.getBillsByVetId(user.userId)));
  }

  // UPDATE BILL - CPC-1827

  updateBill(billId: string, request: BillUpdateRequest): Observable<BillRecord> {
    return this.http
      .put<BillApiRecord>(`${this.baseUrl}/${encodeURIComponent(billId)}`, request)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }

  // ARCHIVE ELIGIBLE BILLS - CPC-1827

  archiveEligibleBills(): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/archive`, null);
  }

  // SHARED SSE HANDLING - CPC-1827

  private getBillsFromUrl(url: string): Observable<BillRecord> {
    return this.sse
      .stream(url, {
        responseType: 'text',
        keepAlive: false,
      })
      .pipe(map((data) => this.toBillRecord(JSON.parse(data) as BillApiRecord)));
  }

  // BACKEND DATE CONVERSION - CPC-1827

  private toBillRecord(bill: BillApiRecord): BillRecord {
    return {
      ...bill,
      date: new Date(`${bill.date}T00:00:00`),
      dueDate: new Date(`${bill.dueDate}T00:00:00`),
    };
  }
}
