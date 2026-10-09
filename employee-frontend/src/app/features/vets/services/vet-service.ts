import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import { Veterinarian } from '@features/vets/models/vet.model';

@Injectable({ providedIn: 'root' })
export class VetService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient)

  getVetByVetId(vetId: string): Observable<Veterinarian> {
    return this.http.get<Veterinarian>(`/api/v2/gateway/vets/${vetId}`);
  }
}