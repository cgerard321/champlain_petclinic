import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Owner, OwnerRequest } from '../models/owner.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root'
})
export class OwnerApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  getOwnersPaginated(_page: number = 0, _size: number = 5): Observable<Owner[]> {
    return this.http.get(this.apiConfig.getFullUrl('/owners', true), {
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
    return this.http.get<number>(`${this.apiConfig.getFullUrl('/owners', false)}/owners-count`, {
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

    return this.http.get<Owner[]>(`${this.apiConfig.getFullUrl('/owners', false)}/owners-pagination`, { 
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

    return this.http.get<number>(`${this.apiConfig.getFullUrl('/owners', false)}/owners-filtered-count`, { 
      params,
      withCredentials: true
    });
  }

 
  getOwners(): Observable<Owner[]> {
    return this.getOwnersPaginated(0, 1000);
  }

 
  getOwnerById(ownerId: string): Observable<Owner> {
    return this.http.get<Owner>(`${this.apiConfig.getFullUrl('/owners', false)}/${ownerId}`, {
      withCredentials: true
    });
  }

  
  createOwner(owner: OwnerRequest): Observable<Owner> {
    return this.http.post<Owner>(`${this.apiConfig.getFullUrl('/owners', true)}`, owner, {
      withCredentials: true
    });
  }

  updateOwner(ownerId: string, owner: OwnerRequest): Observable<Owner> {
    return this.http.put<Owner>(`${this.apiConfig.getFullUrl('/owners', true)}/${ownerId}`, owner, {
      withCredentials: true
    });
  }

  deleteOwner(ownerId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.getFullUrl('/owners', true)}/${ownerId}`, {
      withCredentials: true
    });
  }

 
  getOwnerPets(ownerId: string): Observable<string> {
    return this.http.get(`${this.apiConfig.getFullUrl('/owners', false)}/${ownerId}/pets`, { 
      responseType: 'text',
      withCredentials: true
    });
  }

}

