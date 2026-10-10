import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Bill, BillRequestModel } from '@features/bill/models/bill.model';

@Injectable({ providedIn: 'root' })
export class BillService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/bills';

  createBill(body: BillRequestModel): Observable<Bill> {
    return this.http.post<Bill>(this.baseUrl, body);
  }
}
