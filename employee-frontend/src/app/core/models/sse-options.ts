/**
 * Adapted from ngx-sse-client — https://github.com/marcospds/ngx-sse-client
 * Copyright (c) 2021 Rubens Dos Santos Filho. MIT License (see THIRD_PARTY_NOTICES.md).
 * Modified for this project.
 */

export interface SseOptions {
  /**
   * `true` to automatically reconnect when the request is closed by a request
   * error (including timeout errors) or completed.
   *
   * In this case, unsubscribe manually to close the connection
   * (e.g. `takeUntilDestroyed()` or `toSignal()` inside a component/service).
   *
   * @default `true`
   */
  keepAlive: boolean;

  /**
   * Delay (ms) before reconnecting with the server. Only useful when
   * `keepAlive` is `true`.
   *
   * @default `3000`
   */
  reconnectionDelay: number;

  /**
   * `event`: emits a `MessageEvent` per message and an `ErrorEvent` on errors.
   * `text`: emits only the message data (no errors are emitted).
   *
   * @default `event`
   */
  responseType: 'event' | 'text';

  /**
   * `HttpClient` keeps the whole response text in memory for the lifetime of a
   * request. To stop that from growing forever on long-lived streams, once this
   * many characters have been received (and a message has just finished), the
   * connection is transparently re-opened, sending the `Last-Event-ID` header
   * so the server can resume. Only applies when `keepAlive` is `true`.
   * Set to `0` to disable.
   *
   * @default `5242880` (5 Mi characters)
   */
  maxBufferLength: number;
}

export const defaultSseOptions: SseOptions = {
  keepAlive: true,
  reconnectionDelay: 3_000,
  responseType: 'event',
  maxBufferLength: 5 * 1024 * 1024,
};
