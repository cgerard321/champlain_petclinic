import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Bundle, BundleRequest } from '@features/prod/models/bundle.model';

@Injectable({ providedIn: 'root' })
export class BundleService {
  private readonly http = inject(HttpClient);

  createBundle(request: BundleRequest): Observable<Bundle> {
    return this.http.post<Bundle>('/api/gateway/products/bundles', request);
  }

  updateBundle(bundleId: string, request: BundleRequest): Observable<Bundle> {
    return this.http.put<Bundle>(`/api/gateway/products/bundles/${bundleId}`, request);
  }

  getBundles(): Observable<Bundle[]> {
    return this.http.get<Bundle[]>('/api/gateway/products/bundles', { responseType: 'json' });
  }

  deleteBundle(bundleId: string): Observable<Bundle> {
    return this.http.delete<Bundle>(`/api/gateway/products/bundles/${bundleId}`, {});
  }
}
