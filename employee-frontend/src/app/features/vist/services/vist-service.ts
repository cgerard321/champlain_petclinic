import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { VisitResponseModel } from '@features/vist/models/vist.model';

@Injectable({
  providedIn: 'root',
})
export class VisitService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/visits';

  getVisitById(visitId: string): Observable<VisitResponseModel> {
    return this.http.get<VisitResponseModel>(
      `${this.baseUrl}/${encodeURIComponent(visitId)}`,
    );
  }
}
