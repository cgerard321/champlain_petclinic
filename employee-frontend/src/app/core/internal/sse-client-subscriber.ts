/**
 * Adapted from ngx-sse-client — https://github.com/marcospds/ngx-sse-client
 * Copyright (c) 2021 Rubens Dos Santos Filho. MIT License (see THIRD_PARTY_NOTICES.md).
 * Modified for this project.
 */

import {
  HttpClient,
  HttpDownloadProgressEvent,
  HttpErrorResponse,
  HttpEvent,
  HttpEventType,
  HttpHeaders,
  HttpResponse,
} from '@angular/common/http';
import {
  defer,
  EMPTY,
  Observable,
  repeat,
  retry,
  Subject,
  Subscriber,
  Subscription,
  takeUntil,
  timer,
} from 'rxjs';

import { isApiError } from '@core/models/api-error';
import { SseErrorEvent } from '@core/models/sse-error-event';
import { SseOptions } from '@core/models/sse-options';
import { defaultRequestOptions, SseRequestOptions } from '@core/models/sse-request-options';

type SseHttpOptions = SseRequestOptions & typeof defaultRequestOptions;

type ChunkEvent = { id: string | undefined; data: string; event: string } & Record<
  string,
  string | undefined
>;

/**
 * Internal helper — not injectable. Create one instance per stream via `SseClient`.
 */
export class SseClientSubscriber {
  private static readonly SEPARATOR = ':';

  private progress = 0;
  private chunk = '';
  private lastEventId: string | undefined;

  /** Emits to deliberately close the current request so it can be re-opened. */
  private readonly recycle$ = new Subject<void>();
  private recycling = false;

  constructor(
    private readonly http: HttpClient,
    private readonly sseOptions: SseOptions,
    private readonly httpOptions: SseHttpOptions,
    private readonly url: string,
    private readonly method: string,
  ) {}

  createObservable(): Observable<string | Event> {
    return new Observable<string | Event>((observer) => {
      const subscription = this.subscribeStreamRequest(observer);
      return () => subscription.unsubscribe();
    });
  }

  private subscribeStreamRequest(observer: Subscriber<string | Event>): Subscription {
    const { reconnectionDelay } = this.sseOptions;

    return defer(() => this.http.request(this.method, this.url, this.requestOptions()))
      .pipe(
        takeUntil(this.recycle$),
        repeat({ delay: () => this.nextConnectionDelay() }),
        retry({
          delay: (error: unknown) => {
            const terminal = this.handleRequestError(error, observer);
            return terminal ? EMPTY : timer(reconnectionDelay);
          },
        }),
      )
      .subscribe((event) => this.parseStreamEvent(event, observer));
  }

  /** Re-subscribed on every (re)connection, so `Last-Event-ID` is always current. */
  private requestOptions(): SseHttpOptions {
    if (this.lastEventId === undefined) return this.httpOptions;

    const headers =
      this.httpOptions.headers instanceof HttpHeaders
        ? this.httpOptions.headers
        : new HttpHeaders(this.httpOptions.headers);

    return { ...this.httpOptions, headers: headers.set('Last-Event-ID', this.lastEventId) };
  }

  /** Delay before re-subscribing after the response completed (or was recycled). */
  private nextConnectionDelay(): Observable<number> {
    if (this.recycling) {
      this.recycling = false;
      return timer(0);
    }

    return this.sseOptions.keepAlive ? timer(this.sseOptions.reconnectionDelay) : EMPTY;
  }

  /** @returns `true` when the error is terminal (no reconnection). */
  private handleRequestError(error: unknown, observer: Subscriber<string | Event>): boolean {
    const response = error instanceof HttpErrorResponse ? error : undefined;
    // Without `keepAlive` there is no reconnection, so any failure is terminal too.
    const terminal = !this.sseOptions.keepAlive || !this.isValidStatus(response?.status);

    // Drop any half-received message from the broken connection.
    this.chunk = '';
    this.progress = 0;

    const event: SseErrorEvent = new ErrorEvent('error', {
      error,
      message: this.errorMessage(error),
    });
    if (terminal) {
      // `status`/`statusText` are not part of `ErrorEventInit`, so set them afterwards.
      event.status = response?.status;
      event.statusText = response?.statusText;
    }
    this.dispatchStreamData(event, observer);

    if (terminal) observer.error(error);
    return terminal;
  }

  private errorMessage(error: unknown): string | undefined {
    if (isApiError(error)) return error.message;
    if (error instanceof HttpErrorResponse) return error.message;
    return undefined;
  }

  private isValidStatus(status: number | undefined | null): boolean {
    return status !== undefined && status !== null && status <= 299;
  }

  private parseStreamEvent(event: HttpEvent<string>, observer: Subscriber<string | Event>): void {
    if (event.type === HttpEventType.Sent) {
      this.progress = 0;
      return;
    }

    if (event.type === HttpEventType.DownloadProgress) {
      this.onStreamProgress((event as HttpDownloadProgressEvent).partialText, observer);
      this.recycleIfBufferFull();
      return;
    }

    if (event.type === HttpEventType.Response) {
      this.onStreamCompleted(event as HttpResponse<string>, observer);
    }
  }

  private onStreamProgress(text: string | undefined, observer: Subscriber<string | Event>): void {
    if (!text) return;

    const data = text.substring(this.progress);
    this.progress += data.length;
    data.split(/(\r\n|\r|\n){2}/g).forEach((part) => this.parseEventData(part, observer));
  }

  /**
   * `HttpClient` retains the full `partialText` until the request ends. Once it is
   * large and we are between two messages, close and re-open the request to free it.
   */
  private recycleIfBufferFull(): void {
    const { keepAlive, maxBufferLength } = this.sseOptions;
    const atMessageBoundary = this.chunk === '';

    if (!keepAlive || maxBufferLength <= 0 || !atMessageBoundary) return;
    if (this.progress < maxBufferLength) return;

    this.progress = 0;
    this.recycling = true;
    this.recycle$.next();
  }

  private onStreamCompleted(
    response: HttpResponse<string>,
    observer: Subscriber<string | Event>,
  ): void {
    this.onStreamProgress(response.body ?? undefined, observer);
    this.dispatchStreamData(this.parseEventChunk(this.chunk), observer);

    this.chunk = '';
    this.progress = 0;

    if (this.sseOptions.keepAlive) {
      const message = `Server response ended, will reconnect in ${this.sseOptions.reconnectionDelay}ms`;
      this.dispatchStreamData(
        new ErrorEvent('error', { error: { status: 1, message }, message }),
        observer,
      );
    } else {
      observer.complete();
    }
  }

  private parseEventData(part: string, observer: Subscriber<string | Event>): void {
    if (part.trim().length === 0) {
      this.dispatchStreamData(this.parseEventChunk(this.chunk), observer);
      this.chunk = '';
    } else {
      this.chunk += part;
    }
  }

  private parseEventChunk(chunk: string): MessageEvent | undefined {
    if (!chunk) return undefined;

    const chunkEvent: ChunkEvent = { id: undefined, data: '', event: 'message' };
    chunk.split(/\n|\r\n|\r/).forEach((line) => this.parseChunkLine(line.trim(), chunkEvent));

    if (chunkEvent.id !== undefined) this.lastEventId = chunkEvent.id;

    return new MessageEvent(chunkEvent.event, {
      lastEventId: chunkEvent.id,
      data: chunkEvent.data,
    });
  }

  private parseChunkLine(line: string, event: ChunkEvent): void {
    const index = line.indexOf(SseClientSubscriber.SEPARATOR);
    if (index <= 0) return;

    const field = line.substring(0, index);
    if (!['id', 'data', 'event'].includes(field)) return;

    let data = line.substring(index + 1).replace(/^\s/, '');
    if (field === 'data') data = event.data + data;

    event[field] = data;
  }

  private dispatchStreamData(event: Event | undefined, observer: Subscriber<string | Event>): void {
    if (!this.isValidEvent(event)) return;

    if (this.sseOptions.responseType === 'event') {
      observer.next(event);
    } else {
      observer.next((event as MessageEvent<string>).data);
    }
  }

  private isValidEvent(event: Event | undefined): event is Event {
    if (!event) return false;
    if (event.type === 'error') return this.sseOptions.responseType === 'event';

    const data = (event as MessageEvent<string>).data;
    return !!data && data.length > 0;
  }
}
