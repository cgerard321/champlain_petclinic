import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Visit, VisitRequest, VisitStatus, Owner, Pet, Vet } from '../models/visit.model';
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class VisitApiService {
  private readonly BASE_URL_V1 = `${environment.apiUrl}/visits`;
  private readonly BASE_URL_V2 = `${environment.apiUrlV2}/visits`;
  private http = inject(HttpClient);

  private getBaseUrl(useV2: boolean = false): string {
    return useV2 ? this.BASE_URL_V2 : this.BASE_URL_V1;
  }

  getAllVisits(useV2: boolean = false): Observable<Visit[]> {
    return this.http.get(`${this.getBaseUrl(useV2)}`, {
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

  getVisitsByOwner(ownerId: string, useV2: boolean = false): Observable<Visit[]> {
    return this.http.get(`${this.getBaseUrl(useV2)}/owners/${ownerId}`, {
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

  getVisitsByVet(vetId: string, useV2: boolean = false): Observable<Visit[]> {
    return this.http.get(`${this.getBaseUrl(useV2)}/vets/${vetId}`, {
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

  getVisitById(visitId: string, useV2: boolean = false): Observable<Visit> {
    return this.http.get<Visit>(`${this.getBaseUrl(useV2)}/${visitId}`, {
      withCredentials: true
    });
  }

  getVisitCalendar(practitionerId: string, startDate: string, endDate: string, useV2: boolean = false): Observable<any> {
    return this.http.get<any>(
      `${this.getBaseUrl(useV2)}/calendar/${practitionerId}?dates=${startDate},${endDate}`,
      { withCredentials: true }
    );
  }

  createVisit(visit: VisitRequest, ownerId: string, petId: string, useV2: boolean = false): Observable<Visit> {
    const baseUrl = useV2 ? environment.apiUrlV2 : environment.apiUrl;
    return this.http.post<Visit>(`${baseUrl}/visit/owners/${ownerId}/pets/${petId}/visits`, visit, {
      withCredentials: true
    });
  }

  updateVisit(visitId: string, visit: VisitRequest, petId: string, useV2: boolean = false): Observable<Visit> {
    const baseUrl = useV2 ? environment.apiUrlV2 : environment.apiUrl;
    return this.http.put<Visit>(`${baseUrl}/owners/*/pets/${petId}/visits/${visitId}`, visit, {
      withCredentials: true
    });
  }

  updateVisitStatus(visitId: string, status: VisitStatus, useV2: boolean = false): Observable<Visit> {
    return this.http.put<Visit>(`${this.getBaseUrl(useV2)}/${visitId}/status/${status}`, status, {
      withCredentials: true
    });
  }

  cancelVisit(visitId: string, useV2: boolean = false): Observable<Visit> {
    return this.http.put<Visit>(
      `${this.getBaseUrl(useV2)}/${visitId}/status/${VisitStatus.CANCELLED}`,
      VisitStatus.CANCELLED,
      { withCredentials: true }
    );
  }

  deleteVisit(visitId: string, useV2: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl(useV2)}/${visitId}`, {
      withCredentials: true
    });
  }

  deleteAllCancelledVisits(useV2: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl(useV2)}/cancelled`, {
      withCredentials: true
    });
  }

  getOwners(): Observable<Owner[]> {
    return this.http.get(`${environment.apiUrlV2}/owners`, {
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

  getPetsByOwner(ownerId: string): Observable<Pet[]> {
    return this.http.get(`${environment.apiUrl}/owners/${ownerId}/pets`, {
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

  getVets(): Observable<Vet[]> {
    return this.http.get(`${environment.apiUrlV2}/vets`, {
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
}

