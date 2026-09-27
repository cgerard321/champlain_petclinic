import { signal } from '@angular/core';
import { environment } from '@environments/environment';

export type WsConnectionStatus = 'idle' | 'connecting' | 'open' | 'closed' | 'error';

export class WsConnection {
  private socket: WebSocket | null = null;

  readonly messages = signal<string[]>([]);
  readonly status = signal<WsConnectionStatus>('idle');

  connect(path: string, params?: Record<string, string | number | undefined>): void {
    this.disconnect();
    this.messages.set([]);
    this.status.set('connecting');

    const socket = new WebSocket(this.buildUrl(path, params));
    this.socket = socket;

    socket.onopen = () => this.status.set('open');

    socket.onmessage = (event: MessageEvent<string>) => {
      this.messages.update((current) => [...current, event.data]);
    };

    socket.onerror = () => this.status.set('error');

    socket.onclose = () => {
      if (this.status() !== 'error') {
        this.status.set('closed');
      }
    };
  }

  disconnect(): void {
    this.socket?.close();
    this.socket = null;
    if (this.status() === 'open' || this.status() === 'connecting') {
      this.status.set('closed');
    }
  }

  private buildUrl(path: string, params?: Record<string, string | number | undefined>): string {
    const origin = this.resolveWebSocketOrigin();
    const search = new URLSearchParams();

    if (params) {
      for (const [key, value] of Object.entries(params)) {
        if (value != null) {
          search.set(key, String(value));
        }
      }
    }

    const query = search.toString();
    return `${origin}${path}${query ? `?${query}` : ''}`;
  }

  private resolveWebSocketOrigin(): string {
    const apiUrl = environment.apiUrl;

    if (!apiUrl) {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      return `${protocol}//${window.location.host}`;
    }

    // http:// -> ws://, https:// -> wss://
    return apiUrl.replace(/^http/, 'ws');
  }
}

