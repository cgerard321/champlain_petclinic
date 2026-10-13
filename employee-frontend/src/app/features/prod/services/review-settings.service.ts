import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ReviewSettings } from '../models/review-settings.model';

@Injectable({ providedIn: 'root' })
export class ReviewSettingsService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/gateway/review-settings';

  getSettings() {
    return this.http.get<ReviewSettings>(this.url);
  }

  updateSettings(settings: ReviewSettings) {
    return this.http.put<ReviewSettings>(this.url, settings);
  }
}
