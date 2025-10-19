import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Owner, OwnerRequest } from '../models/owner.model';
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class OwnerApiService {
  private readonly BASE_URL_V1 = `${environment.apiUrl}/owners`; // v1 for individual operations
  private readonly BASE_URL_V2 = `${environment.apiUrlV2}/owners`; // v2 for paginated lists
  private http = inject(HttpClient);

  getOwnersPaginated(_page: number = 0, _size: number = 5): Observable<Owner[]> {
    return this.http.get(`${this.BASE_URL_V2}`, {
      responseType: 'text',
      withCredentials: true
    }).pipe(
      map(response => {
        return response
          .split('data:')
          .map((payload: string) => {
            try {
              if (payload === '') return null;
              return JSON.parse(payload);
            } catch (err) {
              console.error("Can't parse JSON:", err);
              return null;
            }
          })
          .filter((data: any) => data !== null);
      })
    );
  }

 
  getOwnersCount(): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL_V1}/owners-count`, {
      withCredentials: true
    });
  }

  searchOwners(filters: {
    page?: number;
    size?: number;
    ownerId?: string;
    firstName?: string;
    lastName?: string;
    phoneNumber?: string;
    city?: string;
  }): Observable<Owner[]> {
    let params = new HttpParams();
    
    Object.keys(filters).forEach(key => {
      const value = filters[key as keyof typeof filters];
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

    return this.http.get<Owner[]>(`${this.BASE_URL_V1}/owners-pagination`, { 
      params,
      withCredentials: true
    });
  }

  getFilteredOwnersCount(filters: {
    ownerId?: string;
    firstName?: string;
    lastName?: string;
    phoneNumber?: string;
    city?: string;
  }): Observable<number> {
    let params = new HttpParams();
    
    Object.keys(filters).forEach(key => {
      const value = filters[key as keyof typeof filters];
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

    return this.http.get<number>(`${this.BASE_URL_V1}/owners-filtered-count`, { 
      params,
      withCredentials: true
    });
  }

 
  getOwners(): Observable<Owner[]> {
    return this.getOwnersPaginated(0, 1000);
  }

 
  getOwnerById(ownerId: string): Observable<Owner> {
    return this.http.get<Owner>(`${this.BASE_URL_V1}/${ownerId}`, {
      withCredentials: true
    });
  }

  
  createOwner(owner: OwnerRequest): Observable<Owner> {
    return this.http.post<Owner>(`${this.BASE_URL_V2}`, owner, {
      withCredentials: true
    });
  }

  updateOwner(ownerId: string, owner: OwnerRequest): Observable<Owner> {
    return this.http.put<Owner>(`${this.BASE_URL_V2}/${ownerId}`, owner, {
      withCredentials: true
    });
  }

  deleteOwner(ownerId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL_V2}/${ownerId}`, {
      withCredentials: true
    });
  }

 
  getOwnerPets(ownerId: string): Observable<string> {
    return this.http.get(`${this.BASE_URL_V1}/${ownerId}/pets`, { 
      responseType: 'text',
      withCredentials: true
    });
  }

}

