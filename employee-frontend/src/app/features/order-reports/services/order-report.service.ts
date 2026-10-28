import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Order, OrderReport } from '@features/order-reports/models/order-report.model';

@Injectable({ providedIn: 'root' })
export class OrderReportService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v2/gateway/orders';

  getReports(): Observable<OrderReport[]> {
    return this.http.get<OrderReport[]>(`${this.baseUrl}/reports`);
  }

  getOrder(orderId: string): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/${orderId}`);
  }
}
