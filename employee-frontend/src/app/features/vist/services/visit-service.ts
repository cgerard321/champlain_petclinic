import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { EMPTY, filter, map, switchMap, Observable } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import { Pet, Customer } from '@features/cust/models/customer.model';
import { CancellationReason } from '@features/vist/models/CancellationReasons';
import { Status } from '@features/vist/models/Status';
import { TimeSlot } from '@features/vist/models/TimeSlot';
import { Visit } from '@features/vist/models/Visit';
import { VisitResponseModel } from '@features/vist/models/VisitResponseModel';

@Injectable({ providedIn: 'root' })
export class VisitService {
  private readonly sse = inject(SseClient);
  private readonly authState = inject(AuthState);
  private readonly http = inject(HttpClient);

  getAllVisits(): Observable<Visit> {
    const isAdmin = this.authState.hasRole('ADMIN');
    const isVet = this.authState.hasRole('VET');

    if (isAdmin === true) {
      return this.sse.stream('/api/gateway/visits', { keepAlive: false }).pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Visit),
      );
    } else if (isVet === true) {
      return this.http.get<CurrentUserResponse>('/api/gateway/users/jwt').pipe(
        switchMap((user) => {
          const practitionerId = user.userId;

          return this.getVisitsByPractitionerId(practitionerId);
        }),
      );
    } else {
      return EMPTY;
    }
  }

  getVisitsByPractitionerId(practitionerId: string): Observable<Visit> {
    return this.sse
      .stream(`/api/gateway/visits/vets/${practitionerId}/visits`, { keepAlive: false })
      .pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Visit),
      );
  }

  searchVisits(query: string): Observable<Visit> {
    return this.sse
      .stream(`/api/gateway/visits/search?query=${encodeURIComponent(query)}`, {
        keepAlive: false,
      })
      .pipe(
        filter((event): event is MessageEvent<string> => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Visit),
      );
  }

  getVisitById(visitId: string): Observable<VisitResponseModel> {
    return this.http.get<VisitResponseModel>(`/api/gateway/visits/${encodeURIComponent(visitId)}`);
  }

  getAvailableTimeSlots(practitionerId: string, date: string): Observable<TimeSlot[]> {
    return this.http.get<TimeSlot[]>(
      `/api/gateway/visits/availability/vets/${practitionerId}/slots`,
      { params: { date } },
    );
  }

  getAvailableDates(
    practitionerId: string,
    startDate: string,
    endDate: string,
  ): Observable<string[]> {
    return this.http.get<string[]>(
      `/api/gateway/visits/availability/vets/${practitionerId}/dates`,
      { params: { startDate, endDate } },
    );
  }

  addFollowUpVisit(
    completedVisit: Visit,
    visitDate: string,
    description: string,
  ): Observable<Visit> {
    return this.http.post<Visit>('/api/gateway/visits', {
      visitDate,
      description,
      petId: completedVisit.petId,
      practitionerId: completedVisit.practitionerId,
      status: Status.UPCOMING,
      isEmergency: false,
      followUpOfVisitId: completedVisit.visitId,
    });
  }

  getPetById(petId: string): Observable<Pet> {
    return this.http.get<Pet>(`/api/gateway/pets/${petId}`);
  }

  getCustomerById(customerId: string): Observable<Customer> {
    return this.http.get<Customer>(`/api/gateway/customers/${customerId}`);
  }

  addVetNote(visitId: string, vetNote: string): Observable<Visit> {
    return this.http.patch<Visit>(`/api/gateway/visits/${visitId}/note`, { vetNote });
  }

  cancelVisit(visitId: string, details: string): Observable<Visit> {
    return this.http.patch<Visit>(`/api/gateway/visits/${visitId}/cancel`, {
      cancellationReason: CancellationReason.OTHER,
      cancellationReasonDetails: details,
    });
  }
}
