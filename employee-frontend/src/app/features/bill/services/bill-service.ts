import { formatDate } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { EMPTY, map, Observable, switchMap } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import {
  BillRecord,
  BillRequestModel,
  BillUpdateRequest,
  PaymentRequest,
} from '@features/bill/models/bill.model';
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
  createBill(
    body: BillRequestModel,
    sendEmail = false,
    currency: 'CAD' | 'USD' = 'CAD',
  ): Observable<BillRecord> {
    const request = {
      ...body,
      date: formatDate(body.date, 'yyyy-MM-dd', 'en-US'),
    };

    return this.http
      .post<BillApiRecord>(this.baseUrl, request, {
        params: { sendEmail, currency },
      })
      .pipe(map((bill) => this.toBillRecord(bill)));
  }
  getVetByVetId(vetId: string): Observable<Veterinarian> {
    return this.http.get<Veterinarian>(`/api/v2/gateway/vets/${encodeURIComponent(vetId)}`);
  }
  getCustomers(): Observable<Customer> {
    return this.sse
      .stream('/api/gateway/customers', {
        keepAlive: false,
      })
      .pipe(
        map((event) => {
          if (event.type === 'error') {
            throw new Error('Unable to load customers.');
          }

          return JSON.parse((event as MessageEvent<string>).data) as Customer;
        }),
      );
  }
  getAllBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.admin)) {
      return EMPTY;
    }

    return this.getBillsFromUrl(this.baseUrl);
  }
  getBillById(billId: string): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.admin) && !this.auth.hasRole(Roles.vet)) {
      return EMPTY;
    }

    return this.http
      .get<BillApiRecord>(`${this.baseUrl}/${encodeURIComponent(billId)}`)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }
  getBillsByVetId(vetId: string): Observable<BillRecord> {
    return this.getBillsFromUrl(`${this.baseUrl}/vets/${encodeURIComponent(vetId)}`);
  }
  getMyBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.vet)) {
      return EMPTY;
    }

    return this.http
      .get<CurrentUserResponse>('/api/gateway/users/jwt')
      .pipe(switchMap((user) => this.getBillsByVetId(user.userId)));
  }
  updateBill(billId: string, request: BillUpdateRequest): Observable<BillRecord> {
    return this.http
      .put<BillApiRecord>(`${this.baseUrl}/${encodeURIComponent(billId)}`, request)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }
  archiveEligibleBills(): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/archive`, null);
  }
  getReceptionistBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.receptionist)) {
      return EMPTY;
    }

    return this.getBillsFromUrl(this.baseUrl);
  }

  getBillsByCustomerId(customerId: string): Observable<BillRecord> {
    return this.sse
      .stream(`${this.baseUrl}/customer/${encodeURIComponent(customerId)}`, {
        keepAlive: false,
      })
      .pipe(
        map((event) => {
          if (event.type === 'error') {
            throw new Error('Unable to load customer bills.');
          }

          const data = (event as MessageEvent<string>).data;
          return this.toBillRecord(JSON.parse(data) as BillApiRecord);
        }),
      );
  }

  payBill(bill: BillRecord, payment: PaymentRequest): Observable<BillRecord> {
    return this.http
      .post<BillApiRecord>(
        `${this.baseUrl}/customers/${bill.customerId}/${bill.billId}/pay`,
        payment,
      )
      .pipe(map((result) => this.toBillRecord(result)));
  }

  setInterestExempt(billId: string, exempt: boolean): Observable<void> {
    return this.http.patch<void>(
      `${this.baseUrl}/${encodeURIComponent(billId)}/exempt-interest`,
      null,
      {
        params: { exempt },
      },
    );
  }

  downloadBillPdf(billId: string, currency: 'CAD' | 'USD' = 'CAD'): Observable<Blob> {
    return this.http.get(`/api/v2/gateway/bills/${encodeURIComponent(billId)}/pdf`, {
      params: { currency },
      responseType: 'blob',
    });
  }

  private getBillsFromUrl(url: string): Observable<BillRecord> {
    return this.sse
      .stream(url, {
        responseType: 'text',
        keepAlive: false,
      })
      .pipe(map((data) => this.toBillRecord(JSON.parse(data) as BillApiRecord)));
  }
  private toBillRecord(bill: BillApiRecord): BillRecord {
    return {
      ...bill,
      date: new Date(`${bill.date}T00:00:00`),
      dueDate: new Date(`${bill.dueDate}T00:00:00`),
    };
  }
}
