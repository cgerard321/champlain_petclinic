/**
 * Adapted from ngx-sse-client — https://github.com/marcospds/ngx-sse-client
 * Copyright (c) 2021 Rubens Dos Santos Filho. MIT License (see THIRD_PARTY_NOTICES.md).
 * Modified for this project.
 */

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { defaultSseOptions, SseOptions } from '@core/models/sse-options';
import { defaultRequestOptions, SseRequestOptions } from '@core/models/sse-request-options';
import { SseClientSubscriber } from '@core/services/sse-client-subscriber';

@Injectable({ providedIn: 'root' })
export class SseClient {
  private readonly http = inject(HttpClient);

  /**
   * Opens an SSE stream and emits each message as a `MessageEvent`
   * (errors are emitted as `ErrorEvent`s with type `error`).
   *
   * With the default `keepAlive: true` the stream never completes on its own:
   * always tie it to a lifecycle (`takeUntilDestroyed()`, `toSignal()`, ...).
   */
  stream(
    url: string,
    options?: Partial<Omit<SseOptions, 'responseType'>> & { responseType?: 'event' },
    requestOptions?: SseRequestOptions,
    method?: string,
  ): Observable<Event>;

  /**
   * Opens an SSE stream and emits only the message data as a string.
   * No errors are emitted in this mode, only data from successful requests.
   *
   * With the default `keepAlive: true` the stream never completes on its own:
   * always tie it to a lifecycle (`takeUntilDestroyed()`, `toSignal()`, ...).
   */
  stream(
    url: string,
    options: Partial<Omit<SseOptions, 'responseType'>> & { responseType: 'text' },
    requestOptions?: SseRequestOptions,
    method?: string,
  ): Observable<string>;

  stream(
    url: string,
    options?: Partial<SseOptions>,
    requestOptions?: SseRequestOptions,
    method = 'GET',
  ): Observable<string | Event> {
    const sseOptions: SseOptions = { ...defaultSseOptions, ...options };
    const httpOptions = { ...requestOptions, ...defaultRequestOptions };

    return new SseClientSubscriber(
      this.http,
      sseOptions,
      httpOptions,
      url,
      method,
    ).createObservable();
  }
}
