import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { EMPTY, map, Observable } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import { BillRecord, BillUpdateRequest } from '@features/bill/models/bill.model';
import { Roles } from '@shared/models/roles';

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

    return this.http.get<BillRecord>(`/api/gateway/bills/${encodeURIComponent(billId)}`);
  }

  getBillsByVetId(vetId: string): Observable<BillRecord> {
    return this.getBillsFromUrl(`/api/gateway/bills/vets/${encodeURIComponent(vetId)}`);
  }

  private getBillsFromUrl(url: string): Observable<BillRecord> {
    return this.sse
      .stream(url, {
        responseType: 'text',
        keepAlive: false,
      })
      .pipe(map((data) => JSON.parse(data) as BillRecord));
  }

  updateBill(billId: string, request: BillUpdateRequest): Observable<BillRecord> {
    return this.http.put<BillRecord>(`/api/gateway/bills/${encodeURIComponent(billId)}`, request);
  }

  archiveEligibleBills(): Observable<void> {
    return this.http.patch<void>('/api/gateway/bills/archive', null);
  }
}
