import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ReviewSettings } from '@features/prod/models/review-settings.model';

@Injectable({ providedIn: 'root' })
export class ReviewSettingsService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/gateway/review-settings';

  getSettings(): Observable<ReviewSettings> {
    return this.http.get<ReviewSettings>(this.url);
  }

  updateSettings(settings: ReviewSettings): Observable<ReviewSettings> {
    return this.http.put<ReviewSettings>(this.url, settings);
  }
}
