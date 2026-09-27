import { Injectable, computed } from '@angular/core';
import { WsConnection, WsConnectionStatus } from '@core/services/ws-connection';
import { LogMessage } from '@features/docker-logs/models/log-message';
import { TailLogsParams } from '@features/docker-logs/models/tail-log-params';


@Injectable({ providedIn: 'root' })
export class DockerLogs {
  private readonly ws = new WsConnection();

  readonly status = this.ws.status;

  readonly messages = computed<LogMessage[]>(() =>
    this.ws.messages().map((raw) => {
      try {
        return JSON.parse(raw) as LogMessage;
      } catch {
        return { type_name: 'raw', message: raw };
      }
    }),
  );

  getLogsStream(params: TailLogsParams): void {
    const path = `/api/v1/services/${encodeURIComponent(params.service)}/actions/fetch/logs/tail`;

    this.ws.connect(path, {
      container_type: params.containerType,
      number_of_lines: params.numberOfLines,
      db_name: params.dbName,
    });
  }

  stopLogStream(): void {
    this.ws.disconnect();
  }
}
