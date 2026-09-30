/**
 * Adapted from ngx-sse-client — https://github.com/marcospds/ngx-sse-client
 * Copyright (c) 2021 Rubens Dos Santos Filho. MIT License (see THIRD_PARTY_NOTICES.md).
 * Modified for this project.
 */

export interface SseErrorEvent extends ErrorEvent {
  /** HTTP status code from the request error. */
  status?: number;

  /** HTTP status text from the request error. */
  statusText?: string;
}
