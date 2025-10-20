import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Vet, VetRequest } from '../models/vet.model';
import { Education, EducationRequest } from '../models/education.model';
import { Rating, RatingRequest } from '../models/rating.model';
import { Badge } from '../models/badge.model';
import { Photo, PhotoRequest } from '../models/photo.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root',
})
export class VetApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  private getBaseUrl(useV2: boolean = false): string {
    return this.apiConfig.getFullUrl('/vets', useV2);
  }

  getAllVets(useV2: boolean = true): Observable<Vet[]> {
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
                return null;
              }
            })
            .filter((data: unknown) => data !== null);
        })
      );
  }

  getFilteredVets(filterOption?: string, useV2: boolean = false): Observable<Vet[]> {
    let url = this.getBaseUrl(useV2);
    if (filterOption === 'Active') {
      url += '/active';
    } else if (filterOption === 'Inactive') {
      url += '/inactive';
    } else if (filterOption === 'TopVets') {
      url += '/topVets';
    }
    return this.http.get<Vet[]>(url, {
      withCredentials: true,
    });
  }

  getTopVets(useV2: boolean = false): Observable<Vet[]> {
    return this.http.get<Vet[]>(`${this.getBaseUrl(useV2)}/topVets`, {
      withCredentials: true,
    });
  }

  getVetById(vetId: string, useV2: boolean = false): Observable<Vet> {
    return this.http.get<Vet>(`${this.getBaseUrl(useV2)}/${vetId}`, {
      withCredentials: true,
    });
  }

  createVet(vet: VetRequest, useV2: boolean = false): Observable<Vet> {
    return this.http.post<Vet>(`${this.getBaseUrl(useV2)}`, vet, {
      withCredentials: true,
    });
  }

  createVetUser(vetData: Record<string, unknown>, useV2: boolean = false): Observable<Vet> {
    return this.http.post<Vet>(`${this.getBaseUrl(useV2)}/users/vets`, vetData, {
      withCredentials: true,
    });
  }

  updateVet(vetId: string, vet: VetRequest, useV2: boolean = false): Observable<Vet> {
    return this.http.put<Vet>(`${this.getBaseUrl(useV2)}/${vetId}`, vet, {
      withCredentials: true,
    });
  }

  deleteVet(vetId: string, useV2: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl(useV2)}/${vetId}`, {
      withCredentials: true,
    });
  }

  getVetAverageRating(vetId: string, useV2: boolean = false): Observable<number> {
    return this.http.get<number>(`${this.getBaseUrl(useV2)}/${vetId}/ratings/average`, {
      withCredentials: true,
    });
  }

  getVetRatingCount(vetId: string, useV2: boolean = false): Observable<number> {
    return this.http.get<number>(`${this.getBaseUrl(useV2)}/${vetId}/ratings/count`, {
      withCredentials: true,
    });
  }

  getVetRatings(vetId: string, useV2: boolean = true): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${this.getBaseUrl(useV2)}/${vetId}/ratings`, {
      withCredentials: true,
    });
  }

  getVetRatingsPercentages(vetId: string, useV2: boolean = false): Observable<unknown> {
    return this.http.get<unknown>(`${this.getBaseUrl(useV2)}/${vetId}/ratings/percentages`, {
      withCredentials: true,
    });
  }

  getVetRatingsByDate(
    vetId: string,
    year: number,
    month?: number,
    useV2: boolean = false
  ): Observable<Rating[]> {
    let params = new HttpParams().set('year', year.toString());
    if (month !== undefined) {
      params = params.set('month', month.toString());
    }
    return this.http.get<Rating[]>(`${this.getBaseUrl(useV2)}/${vetId}/ratings/date`, {
      params,
      withCredentials: true,
    });
  }

  createVetRating(
    vetId: string,
    rating: RatingRequest,
    useV2: boolean = false
  ): Observable<Rating> {
    return this.http.post<Rating>(`${this.getBaseUrl(useV2)}/${vetId}/ratings`, rating, {
      withCredentials: true,
    });
  }

  updateVetRating(
    vetId: string,
    ratingId: string,
    rating: RatingRequest,
    useV2: boolean = false
  ): Observable<Rating> {
    return this.http.put<Rating>(`${this.getBaseUrl(useV2)}/${vetId}/ratings/${ratingId}`, rating, {
      withCredentials: true,
    });
  }

  deleteVetRating(vetId: string, ratingId: string, useV2: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl(useV2)}/${vetId}/ratings/${ratingId}`, {
      withCredentials: true,
    });
  }

  getVetEducations(vetId: string, useV2: boolean = false): Observable<Education[]> {
    return this.http.get<Education[]>(`${this.getBaseUrl(useV2)}/${vetId}/educations`, {
      withCredentials: true,
    });
  }

  createVetEducation(
    vetId: string,
    education: EducationRequest,
    useV2: boolean = false
  ): Observable<Education> {
    return this.http.post<Education>(`${this.getBaseUrl(useV2)}/${vetId}/educations`, education, {
      withCredentials: true,
    });
  }

  updateVetEducation(
    vetId: string,
    educationId: string,
    education: EducationRequest,
    useV2: boolean = false
  ): Observable<Education> {
    return this.http.put<Education>(
      `${this.getBaseUrl(useV2)}/${vetId}/educations/${educationId}`,
      education,
      {
        withCredentials: true,
      }
    );
  }

  deleteVetEducation(vetId: string, educationId: string, useV2: boolean = false): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl(useV2)}/${vetId}/educations/${educationId}`, {
      withCredentials: true,
    });
  }

  getVetBadge(vetId: string, useV2: boolean = false): Observable<Badge> {
    return this.http.get<Badge>(`${this.getBaseUrl(useV2)}/${vetId}/badge`, {
      withCredentials: true,
    });
  }

  getVetPhoto(vetId: string, useV2: boolean = false): Observable<Photo> {
    return this.http.get<Photo>(`${this.getBaseUrl(useV2)}/${vetId}/photo`, {
      withCredentials: true,
    });
  }

  getVetDefaultPhoto(vetId: string, useV2: boolean = false): Observable<Photo> {
    return this.http.get<Photo>(`${this.getBaseUrl(useV2)}/${vetId}/default-photo`, {
      withCredentials: true,
    });
  }

  uploadVetPhoto(
    vetId: string,
    imageName: string,
    imageData: PhotoRequest,
    useV2: boolean = false
  ): Observable<Photo> {
    return this.http.post<Photo>(
      `${this.getBaseUrl(useV2)}/${vetId}/photos/${imageName}`,
      imageData,
      {
        withCredentials: true,
      }
    );
  }

  updateVetPhoto(
    vetId: string,
    imageName: string,
    imageData: PhotoRequest,
    useV2: boolean = false
  ): Observable<Photo> {
    return this.http.put<Photo>(
      `${this.getBaseUrl(useV2)}/${vetId}/photos/${imageName}`,
      imageData,
      {
        withCredentials: true,
      }
    );
  }

  getVetVisits(vetId: string): Observable<unknown[]> {
    return this.http
      .get(this.apiConfig.getFullUrl(`/visits/vets/${vetId}/visits`), {
        responseType: 'text',
        withCredentials: true,
      })
      .pipe(
        map((response: string) => {
          return response
            .split('data:')
            .map((dataChunk: string) => {
              try {
                if (dataChunk === '') return null;
                return JSON.parse(dataChunk);
              } catch (err) {
                return null;
              }
            })
            .filter((data: unknown) => data !== null);
        })
      );
  }
}
