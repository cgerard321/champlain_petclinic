import { Injectable, inject } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable, map } from 'rxjs';
import { MonitoredService } from '@features/docker-logs/models/monitored-service';
import { MONITORED_SERVICES_QUERY } from '@features/docker-logs/services/graphql/queries/monitored_services_query';

interface MonitoredServicesResponse {
  queryMonitoredServices: MonitoredService[];
}

@Injectable({ providedIn: 'root' })
export class DockerServices {
  private readonly apollo = inject(Apollo);

  listMonitoredServices(): Observable<MonitoredService[]> {
    return this.apollo
      .query<MonitoredServicesResponse>({ query: MONITORED_SERVICES_QUERY })
      .pipe(map((result) => result.data?.queryMonitoredServices ?? []));
  }
}
