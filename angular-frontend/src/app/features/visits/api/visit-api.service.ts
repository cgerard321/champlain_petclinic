import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Visit, VisitRequest, VisitStatus, Owner, Pet, Vet } from '../models/visit.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root',
})
export class VisitApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  private getBaseUrl(useV2: boolean = false): string {
    return this.apiConfig.getFullUrl('/visits', useV2);
  }

  getAllVisits(useV2: boolean = false): Observable<Visit[]> {
    return this.http
      .get(`${this.getBaseUrl(useV2)}`, {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
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
            .filter((data: unknown): data is Visit => data !== null);
        })
      );
  }

  getVisitsByOwner(ownerId: string, useV2: boolean = false): Observable<Visit[]> {
    return this.http
      .get(`${this.getBaseUrl(useV2)}/owners/${ownerId}`, {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
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
            .filter((data: unknown): data is Visit => data !== null);
        })
      );
  }

  getVisitsByVet(vetId: string, useV2: boolean = false): Observable<Visit[]> {
    return this.http
      .get(`${this.getBaseUrl(useV2)}/vets/${vetId}`, {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
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
            .filter((data: unknown): data is Visit => data !== null);
        })
      );
  }

  getVisitById(visitId: string, useV2: boolean = false): Observable<Visit> {
    return this.http.get<Visit>(`${this.getBaseUrl(useV2)}/${visitId}`, {
      withCredentials: true,
    });
  }

  getVisitCalendar(
    practitionerId: string,
    startDate: string,
    endDate: string,
    useV2: boolean = false
  ): Observable<unknown> {
    return this.http.get<unknown>(
      `${this.getBaseUrl(useV2)}/calendar/${practitionerId}?dates=${startDate},${endDate}`,
      { withCredentials: true }
    );
  }

  createVisit(
    visit: VisitRequest,
    ownerId: string,
    petId: string,
    useV2: boolean = false
  ): Observable<Visit> {
    return this.http.post<Visit>(
      this.apiConfig.getFullUrl(`/visit/owners/${ownerId}/pets/${petId}/visits`, useV2),
      visit,
      {
        withCredentials: true,
      }
    );
  }

  updateVisit(
    visitId: string,
    visit: VisitRequest,
    petId: string,
    useV2: boolean = false
  ): Observable<Visit> {
    return this.http.put<Visit>(
      this.apiConfig.getFullUrl(`/owners/*/pets/${petId}/visits/${visitId}`, useV2),
      visit,
      {
        withCredentials: true,
      }
    );
  }

  updateVisitStatus(
    visitId: string,
    status: VisitStatus,
    useV2: boolean = false
  ): Observable<Visit> {
    return this.http.put<Visit>(`${this.getBaseUrl(useV2)}/${visitId}/status/${status}`, status, {
      withCredentials: true,
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
      withCredentials: true,
    });
  }

  deleteAllCancelledVisits(useV2: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl(useV2)}/cancelled`, {
      withCredentials: true,
    });
  }

  getOwners(): Observable<Owner[]> {
    return this.http
      .get(this.apiConfig.getFullUrl('/owners', true), {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
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
            .filter((data: unknown): data is Owner => data !== null);
        })
      );
  }

  getPetsByOwner(ownerId: string): Observable<Pet[]> {
    return this.http
      .get(this.apiConfig.getFullUrl(`/owners/${ownerId}/pets`), {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
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
            .filter((data: unknown): data is Pet => data !== null);
        })
      );
  }

  getVets(): Observable<Vet[]> {
    return this.http
      .get(this.apiConfig.getFullUrl('/vets', true), {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
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
            .filter((data: unknown): data is Vet => data !== null);
        })
      );
  }
}
