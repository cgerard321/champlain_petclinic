import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
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
    return this.http.get<Bill[]>(`${this.BASE_URL}`, { 
      params,
      withCredentials: true
    });
  }

  getAllBillsPaginated(page: number = 0, size: number = 10): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.BASE_URL}?page=${page}&size=${size}`, {
      withCredentials: true
    });
  }

  getBillsCount(): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/bills-count`, {
      withCredentials: true
    });
  }

  getBillsByCustomer(customerId: string): Observable<Bill[]> {
    return this.http.get(`${this.BASE_URL}/customer/${customerId}`, {
      responseType: 'text',
      withCredentials: true
    }).pipe(
      map(response => {
        return response
          .split('data:')
          .map((dataChunk: string) => {
            try {
              if (dataChunk === '') return null;
              return JSON.parse(dataChunk);
            } catch (error) {
              return null;
            }
          })
          .filter((data: any) => data !== null);
      })
    );
  }

  getBillsByVet(vetId: string): Observable<Bill[]> {
    return this.http.get(`${this.BASE_URL}/vet/${vetId}`, {
      responseType: 'text',
      withCredentials: true
    }).pipe(
      map(response => {
        return response
          .split('data:')
          .map((dataChunk: string) => {
            try {
              if (dataChunk === '') return null;
              return JSON.parse(dataChunk);
            } catch (error) {
              return null;
            }
          })
          .filter((data: any) => data !== null);
      })
    );
  }

  getBillById(billId: string): Observable<Bill> {
    return this.http.get<Bill>(`${this.BASE_URL}/${billId}`, {
      withCredentials: true
    });
  }

  createBill(bill: BillRequest): Observable<Bill> {
    return this.http.post<Bill>(`${this.BASE_URL}`, bill, {
      withCredentials: true
    });
  }

  updateBill(billId: string, bill: BillRequest): Observable<Bill> {
    return this.http.put<Bill>(`${this.BASE_URL}/${billId}`, bill, {
      withCredentials: true
    });
  }

  deleteBill(billId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${billId}`, {
      withCredentials: true
    });
  }

  deleteAllBillsByVet(vetId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/vet/${vetId}`, {
      withCredentials: true
    });
  }

  deleteAllBillHistory(): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}`, {
      withCredentials: true
    });
  }

  getBillsByOwnerId(ownerId: string): Observable<Bill[]> {
    return this.getBillsByCustomer(ownerId);
  }

  getBillsByVetId(vetId: string): Observable<Bill[]> {
    return this.getBillsByVet(vetId);
  }

  searchBills(searchTerm: string): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.BASE_URL}/search`, {
      params: { q: searchTerm },
      withCredentials: true
    });
  }

  getBillsByStatus(status: string): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.BASE_URL}/${status.toLowerCase()}`, {
      withCredentials: true
    });
  }
}
