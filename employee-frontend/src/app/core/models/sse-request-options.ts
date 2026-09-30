/**
 * Adapted from ngx-sse-client — https://github.com/marcospds/ngx-sse-client
 * Copyright (c) 2021 Rubens Dos Santos Filho. MIT License (see THIRD_PARTY_NOTICES.md).
 * Modified for this project.
 */

import { HttpContext, HttpHeaders, HttpParams } from '@angular/common/http';

export interface SseRequestOptions {
  body?: unknown;
  headers?: HttpHeaders | Record<string, string | string[]>;
  params?: HttpParams | Record<string, string | string[]>;
  context?: HttpContext;
  withCredentials?: boolean;
}

export const defaultRequestOptions = {
  observe: 'events',
  reportProgress: true,
  responseType: 'text',
} as const;
