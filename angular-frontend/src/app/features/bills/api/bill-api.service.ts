import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bill, BillRequest } from '../models/bill.model';
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class BillApiService {
  private readonly BASE_URL = `${environment.apiUrl}/bills`;
  private http = inject(HttpClient);

  
  getAllBills(page: number = 0, size: number = 10): Observable<Bill[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Bill[]>(`${this.BASE_URL}`, { params });
  }

  
  getBillsCount(): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/bills-count`);
  }

  
  getBillsByCustomer(customerId: string): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.BASE_URL}/customer/${customerId}`);
  }

  
  getBillsByVet(vetId: string): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.BASE_URL}/vet/${vetId}`);
  }

  
  getBillById(billId: string): Observable<Bill> {
    return this.http.get<Bill>(`${this.BASE_URL}/${billId}`);
  }

  
  createBill(bill: BillRequest): Observable<Bill> {
    return this.http.post<Bill>(`${this.BASE_URL}`, bill);
  }

  
  updateBill(billId: string, bill: BillRequest): Observable<Bill> {
    return this.http.put<Bill>(`${this.BASE_URL}/${billId}`, bill);
  }

  
  deleteBill(billId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${billId}`);
  }

  
  deleteAllBillsByVet(vetId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/vet/${vetId}`);
  }

  
  deleteAllBillHistory(): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}`);
  }

  
  getBillsByOwnerId(ownerId: string): Observable<Bill[]> {
    return this.getBillsByCustomer(ownerId);
  }

  
  getBillsByVetId(vetId: string): Observable<Bill[]> {
    return this.getBillsByVet(vetId);
  }

}
