import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { EMPTY, filter, map, switchMap, Observable } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';
import { SseClient } from '@core/services/sse-client';
import { Visit } from '@features/vist/models/Visit';

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
}
