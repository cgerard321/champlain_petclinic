import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, toArray } from 'rxjs';

import { isApiError } from '@core/models/api-error';
import { SseClient } from '@core/services/sse-client';
import {
  VetResponse,
  VetUpdateRequest,
  VetCreateRequest,
  VetEducationRequest,
  VetEducationResponse,
} from '@features/vets/models/vet-api.model';

@Injectable({ providedIn: 'root' })
export class VetService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);
  private readonly baseUrl = '/api/v2/gateway/vets';

  getVets(): Observable<VetResponse[]> {
    return this.sse
      .stream(
        this.baseUrl,
        { responseType: 'text', keepAlive: false },
        { headers: { Accept: 'text/event-stream' } },
      )
      .pipe(
        map((data) => JSON.parse(data) as VetResponse),
        toArray(),
      );
  }

  getVetById(vetId: string): Observable<VetResponse> {
    return this.http.get<VetResponse>(`${this.baseUrl}/${encodeURIComponent(vetId)}`);
  }

  updateVet(vetId: string, request: VetUpdateRequest): Observable<VetResponse> {
    return this.http.put<VetResponse>(`${this.baseUrl}/${encodeURIComponent(vetId)}`, request);
  }

  deleteVet(vetId: string): Observable<VetResponse> {
    return this.http.delete<VetResponse>(`${this.baseUrl}/${encodeURIComponent(vetId)}`);
  }

  addSpecialty(vetId: string, name: string): Observable<VetResponse> {
    return this.http.post<VetResponse>(`${this.baseUrl}/${encodeURIComponent(vetId)}/specialties`, {
      name,
    });
  }

  getEducations(vetId: string): Observable<VetEducationResponse[]> {
    return this.http.get<VetEducationResponse[]>(`${this.baseUrl}/${vetId}/educations`, {
      headers: {
        Accept: 'application/json',
      },
    });
  }
}

export function vetErrorMessage(error: unknown): string {
  const code =
    error instanceof HttpErrorResponse ? error.status : isApiError(error) ? error.code : '';
  if (code === 404 || code === 'NOT_FOUND') {
    return $localize`:@@vets.errors.not_found:Ce profil vétérinaire est introuvable.`;
  }
  if (code === 403 || code === 'FORBIDDEN') {
    return $localize`:@@vets.errors.forbidden:Vous n'avez pas la permission d'effectuer cette action.`;
  }
  if (code === 401 || code === 'UNAUTHORIZED') {
    return $localize`:@@vets.errors.unauthorized:Votre session a expiré. Veuillez vous reconnecter.`;
  }
  if (code === 400 || code === 422 || code === 'BAD_REQUEST' || code === 'UNPROCESSABLE_ENTITY') {
    return $localize`:@@vets.errors.invalid:Les informations ont été refusées. Vérifiez les champs et réessayez.`;
  }
  return $localize`:@@vets.errors.generic:Impossible de terminer la demande. Vérifiez votre connexion et réessayez.`;
}
