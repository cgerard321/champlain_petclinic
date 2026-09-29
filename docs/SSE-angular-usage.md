# SSE client (`SseClient`)

A small Server-Sent Events client built on Angular's `HttpClient`, so every SSE request goes through your interceptors (base URL, cookies/credentials, error mapping, 401 redirect). It replaces the native `EventSource`, which can't send custom headers or use POST.

Adapted from [ngx-sse-client](https://github.com/marcospds/ngx-sse-client) (MIT, © 2021 Rubens Dos Santos Filho). See `THIRD_PARTY_NOTICES.md`.

---

## Where things live

```
src/app/core/
├── models/
│   ├── sse-options.ts            # SseOptions + defaults
│   ├── sse-request-options.ts    # headers / params / body / context for the request
│   └── sse-error-event.ts        # ErrorEvent + status / statusText
├── services/
│   └── sse-client.ts             # ← the only thing you inject
└── internal/
    └── sse-client-subscriber.ts  # private to core: parsing, reconnect, buffer handling
```

Only inject `SseClient`. `SseClientSubscriber` is a plain (non-injectable) class that `SseClient` creates once per stream, so it lives in `internal/` rather than `services/`. Nothing outside `core/` may import from `internal/` (enforced by a `no-restricted-imports` rule in `eslint.config.mjs`). Per the architecture guide, features consume it via DI from **their own feature service**, never directly from a page.

---

## Quick start

### 1. Wrap the stream in a feature service

```ts
// features/inventories/services/inventory-service.ts
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, throwError, timer } from 'rxjs';
import { filter, map, retry } from 'rxjs/operators';

import { SseClient } from '@core/services/sse-client';
import { Inventory } from '@features/inventories/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getInventories(): Observable<Inventory> {
      return this.sse.stream(this.baseUrl, { keepAlive: false }).pipe(
          filter((event): event is MessageEvent => event.type !== 'error'),
          map((event) => JSON.parse(event.data) as Inventory),
          // Retry only network failures (status 0), up to 5 times.
          // Any HTTP error (401, 403, 5xx, ...) goes straight to the caller.
          retry({
              count: 5,
              delay: (error: unknown) => {
                  // We try to cast it to get the error code or the status
                  const err = error as { code?: number; status?: number };
                  const isNetworkError =
                      error instanceof HttpErrorResponse
                          ? error.status === 0
                          : err?.code === 0 || err?.status === 0;
                  return isNetworkError ? timer(5000) : throwError(() => error);
                  },
          }),
      )

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }
}
```

Use a **relative URL**: `apiBaseUrlInterceptor` prepends the API host and sets `withCredentials`, so cookie auth works on the stream.

### 2. Consume it in the page, and always tie it to a lifecycle

```ts
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

export class InventoryList {
  private readonly inventoryService = inject(InventoryService);

  // Unsubscribes automatically when the component is destroyed
  protected readonly latest = toSignal(this.inventoryService.getInventories());
}
```

Or with a manual subscription:

```ts
private readonly destroyRef = inject(DestroyRef);

ngOnInit(): void {
  this.inventoryService
    .getInventories()
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({ next: (inv) => { /* ... */ }, error: (err) => { /* ... */ } });
}
```

> With the default `keepAlive: true` the stream **never completes on its own**. If you forget to unsubscribe it keeps reconnecting forever.

**Where the lifecycle belongs:** put `takeUntilDestroyed` / `toSignal` in the **page or component** that subscribes, not in the `providedIn: 'root'` service. A root service outlives every component, so `takeUntilDestroyed` there would only fire when the whole app is torn down.

---

## API

```ts
sse.stream(url, options?, requestOptions?, method = 'GET')
```

| Param            | Type                  | Description                                                                    |
|------------------|-----------------------|--------------------------------------------------------------------------------|
| `url`            | `string`              | Endpoint (relative URLs get the API base URL).                                 |
| `options`        | `Partial<SseOptions>` | Stream behaviour, see below.                                                   |
| `requestOptions` | `SseRequestOptions`   | `headers`, `params`, `body`, `context`, `withCredentials`.                     |
| `method`         | `string`              | HTTP method. Use `'POST'` with `requestOptions.body` if the endpoint needs it. |

The return type depends on `responseType`: `Observable<Event>` for `'event'`, `Observable<string>` for `'text'`.

### `SseOptions`

| Option              | Default     | Description                                                                                                                         |
|---------------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `keepAlive`         | `true`      | Reconnect automatically after the response ends or a recoverable error. Set `false` for finite streams that end after sending data. |
| `reconnectionDelay` | `3000`      | Milliseconds to wait before reconnecting (only with `keepAlive`).                                                                   |
| `responseType`      | `'event'`   | `'event'` emits `MessageEvent`s (plus `ErrorEvent`s). `'text'` emits only the message `data` string.                                |
| `maxBufferLength`   | `5_242_880` | Characters received before the connection is transparently recycled (see [Long-lived streams](#long-lived-streams)). `0` disables.  |

---

## Response types

### `'event'` (default)

Each message arrives as a `MessageEvent`:

- `event.type` is the SSE `event:` field, or `'message'` if the server didn't send one.
- `event.data` is the `data:` payload (a string, so `JSON.parse` it yourself).
- `event.lastEventId` is the `id:` field, if sent.

Errors and reconnect notices arrive as `ErrorEvent`s with `type === 'error'`, so **filter them out** if you only want data:

```ts
filter((event): event is MessageEvent => event.type !== 'error')
```

For terminal HTTP errors the `ErrorEvent` also has `status` and `statusText` (cast to `SseErrorEvent`).

### `'text'`

Only the `data` string of each message. No error events are emitted; failures still surface through the Observable's `error` channel.

```ts
this.sse.stream('/api/updates', { responseType: 'text' }).subscribe((data) => console.log(data));
```

Messages with empty `data` are ignored.

---

## Reconnection and errors

| Situation                                                  | `keepAlive: true` (default)                                                                                   | `keepAlive: false`                                    |
|------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| Server closes the response normally                        | Emits an `ErrorEvent` ("Server response ended, will reconnect in …ms"), waits `reconnectionDelay`, reconnects | Observable **completes**                              |
| Network failure (status `0`)                               | Emits an `ErrorEvent`, reconnects after the delay                                                             | Emits an `ErrorEvent`, then the Observable **errors** |
| HTTP error (4xx/5xx) or `ApiError` from `errorInterceptor` | Emits an `ErrorEvent` (with `status`), then the Observable **errors**, with no reconnect                      | Same                                                  |
| `401`                                                      | `authInterceptor` navigates to `/login`, then the Observable errors                                           | Same                                                  |

The error you receive in `subscribe({ error })` is whatever your interceptors produced: an `ApiError` when the backend returned a `message`, otherwise the original `HttpErrorResponse`. Use `isApiError()` from `core/models/api-error` to tell them apart.

### Retrying with `keepAlive: false`

With `keepAlive: false` the client never reconnects on its own, so add a `retry` in the feature service if you want one. **Only retry failures that can succeed later** (network drops), cap the attempts, and let everything else reach the caller so the page can show its error state:

```ts
retry({
  count: 5,
  delay: (error: unknown) =>
    error instanceof HttpErrorResponse && error.status === 0
      ? timer(5000)
      : throwError(() => error),
}),
```

Avoid `retry({ count: Infinity })` with no condition: errors never reach `subscribe({ error })`, so the UI stays on its loading state, and a 401/403/404 is re-requested every few seconds until the component is destroyed. An `ApiError` from `errorInterceptor` has no HTTP status, so the condition above does not retry it.

---

## Resuming: `Last-Event-ID`

If the server sends `id:` fields, the client remembers the last one and sends it as the `Last-Event-ID` header on every reconnect, so a server that supports it can resume where you left off.

- Your backend must read that header. If it doesn't, reconnects simply start over.
- Cross-origin requests need `Last-Event-ID` in `Access-Control-Allow-Headers`.

---

## Long-lived streams

`HttpClient` keeps the entire response text in memory until the request ends, so a stream open for hours would grow without bound. To prevent that, once `maxBufferLength` characters have been received **and a message has just finished**, the client closes the request and immediately re-opens it, sending `Last-Event-ID`.

- Only applies when `keepAlive: true`.
- If your server ignores `Last-Event-ID`, a recycle may replay data or miss events in the gap. Set `maxBufferLength: 0` to disable it.
- Finite streams (like inventories) never reach the limit, so it never triggers.

---

## Testing

Use `HttpTestingController`. **Always pass `keepAlive: false` in tests**, otherwise the stream schedules a reconnect and never completes.

```ts
service.getInventories().subscribe((result) => expect(result).toEqual(inventory));

const request = http.expectOne('/api/gateway/inventories');
expect(request.request.method).toBe('GET');

request.flush(`data: ${JSON.stringify(inventory)}`);
```

For multiple messages, separate them with a blank line:

```ts
request.flush('data: {"a":1}\n\nevent: update\ndata: {"a":2}\n\n');
```

---

## Things to know

- **Multi-line `data:`:** consecutive `data:` lines are joined with **no newline** (unlike the SSE spec, which joins with `\n`). Send each payload on a single `data:` line, e.g. JSON on one line.
- **Comments and unknown fields:** lines starting with `:` and fields other than `id`, `data`, `event` are ignored. The `retry:` field is not honoured; use `reconnectionDelay` instead.
- **Interceptors run on every (re)connect,** so an expired session hits `authInterceptor` as usual.
- **Each `stream()` call opens its own connection.** Share one with `shareReplay({ bufferSize: 1, refCount: true })` if several places need the same stream.
- **Backend/proxy:** the endpoint must respond with `Content-Type: text/event-stream`, and any proxy (nginx, gateway) must not buffer the response or the messages will arrive in bursts.