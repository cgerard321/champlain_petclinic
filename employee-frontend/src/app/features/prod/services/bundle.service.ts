import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Bundle } from '@features/prod/models/bundle.model';

@Injectable({ providedIn: 'root' })
export class BundleService {
  private readonly http = inject(HttpClient);

  getBundles(): Observable<Bundle[]> {
    return this.http.get<Bundle[]>('/api/gateway/products/bundles', { responseType: 'json' });
  }

  deleteBundle(bundleId: string): Observable<Bundle> {
    return this.http.delete<Bundle>(`/api/gateway/products/bundles/${bundleId}`, {});
  }
}
