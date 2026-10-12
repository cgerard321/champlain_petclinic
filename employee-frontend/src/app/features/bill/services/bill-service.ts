import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { EMPTY, map, Observable, switchMap } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import { BillRecord, BillUpdateRequest } from '@features/bill/models/bill.model';
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

  getAllBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.admin)) {
      return EMPTY;
    }

    return this.getBillsFromUrl('/api/gateway/bills');
  }

  getBillById(billId: string): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.admin) && !this.auth.hasRole(Roles.vet)) {
      return EMPTY;
    }

    return this.http
      .get<BillApiRecord>(`/api/gateway/bills/${encodeURIComponent(billId)}`)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }

  getBillsByVetId(vetId: string): Observable<BillRecord> {
    return this.getBillsFromUrl(`/api/gateway/bills/vets/${encodeURIComponent(vetId)}`);
  }

  getMyBills(): Observable<BillRecord> {
    if (!this.auth.hasRole(Roles.vet)) {
      return EMPTY;
    }

    return this.http
      .get<CurrentUserResponse>('/api/gateway/users/jwt')
      .pipe(switchMap((user) => this.getBillsByVetId(user.userId)));
  }

  private getBillsFromUrl(url: string): Observable<BillRecord> {
    return this.sse
      .stream(url, {
        responseType: 'text',
        keepAlive: false,
      })
      .pipe(map((data) => this.toBillRecord(JSON.parse(data) as BillApiRecord)));
  }

  updateBill(billId: string, request: BillUpdateRequest): Observable<BillRecord> {
    return this.http
      .put<BillApiRecord>(`/api/gateway/bills/${encodeURIComponent(billId)}`, request)
      .pipe(map((bill) => this.toBillRecord(bill)));
  }

  archiveEligibleBills(): Observable<void> {
    return this.http.patch<void>('/api/gateway/bills/archive', null);
  }

  private toBillRecord(bill: BillApiRecord): BillRecord {
    return {
      ...bill,
      date: new Date(`${bill.date}T00:00:00`),
      dueDate: new Date(`${bill.dueDate}T00:00:00`),
    };
  }
}
