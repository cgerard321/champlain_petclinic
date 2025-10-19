import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiConfigService {
  private readonly baseUrl: string;
  private readonly baseUrlV2: string;

  constructor() {
    // Use Vite environment variables with fallbacks (same as React)
    this.baseUrl = import.meta.env.VITE_BACKEND_URL || '/api/gateway';
    this.baseUrlV2 = import.meta.env.VITE_BACKEND_URL_V2 || '/api/v2/gateway';
  }

  getBaseUrl(useV2: boolean = false): string {
    return useV2 ? this.baseUrlV2 : this.baseUrl;
  }

  getFullUrl(endpoint: string, useV2: boolean = false): string {
    const base = this.getBaseUrl(useV2);
    return `${base}${endpoint}`;
  }
}
