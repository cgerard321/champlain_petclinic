import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { SQLDialect, StandardSQL, MySQL, PostgreSQL } from '@codemirror/lang-sql';

import { DockerServices } from '@features/docker-logs/services/docker-services';
import { MonitoredService, ServiceDb } from '@features/docker-logs/models/monitored-service';
import { QueryLanguage } from '@features/db-consoles/models/query-language';
import { QueryEditor } from '@features/db-consoles/components/query-editor/query-editor';
import { SqlResultsTable } from '@features/db-consoles/components/sql-result-table/sql-results-table';
import { MongoResultsTable } from '@features/db-consoles/components/mongo-result-table/mongo-results-table';
import { SqlResult } from '@features/db-consoles/models/sql-result';
import { MongoResult } from '@features/db-consoles/models/mongo-result';
import { DbConsole } from '@features/db-consoles/services/db-console';
import { Observable } from 'rxjs';


function resolveQueryLanguage(dbType: string | null | undefined): QueryLanguage {
  return dbType === 'Mongo' ? 'mongo' : 'sql';
}

function resolveSqlDialect(dbType: string | null | undefined): SQLDialect {
  switch (dbType) {
    case 'MySQL':
      return MySQL;
    case 'Postgres':
      return PostgreSQL;
    default:
      return StandardSQL;
  }
}

@Component({
  selector: 'app-query-console',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatIcon,
    MatProgressSpinnerModule,
    MatSelectModule,
    QueryEditor,
    SqlResultsTable,
    MongoResultsTable,
  ],
  templateUrl: './query-console.html',
  styleUrl: './query-console.css',
})
export class QueryConsole {
  private readonly dockerServices = inject(DockerServices);
  private readonly dbConsole = inject(DbConsole);

  protected readonly servicesResource = rxResource({
    stream: () => this.dockerServices.listMonitoredServices(),
  });
  protected readonly services = computed(() => this.servicesResource.value() ?? []);
  protected readonly isLoadingServices = computed(() => this.servicesResource.isLoading());
  protected readonly loadError = computed(() =>
    this.servicesResource.error() ? 'Failed to load monitored services.' : null,
  );

  protected readonly selectedServiceName = signal<string | null>(null);
  protected readonly selectedDbName = signal<string | null>(null);
  protected queryText = signal('');

  protected readonly isRunning = signal(false);
  protected readonly runError = signal<string | null>(null);
  protected readonly sqlResult = signal<SqlResult | null>(null);
  protected readonly mongoResult = signal<MongoResult | null>(null);

  protected readonly selectedService = computed<MonitoredService | null>(
    () => this.services().find((s) => s.name === this.selectedServiceName()) ?? null,
  );
  protected readonly availableDbs = computed<ServiceDb[]>(() => this.selectedService()?.dbs ?? []);

  protected readonly selectedDb = computed<ServiceDb | null>(() => {
    const dbs = this.availableDbs();
    if (dbs.length === 1) {
      return dbs[0];
    }
    return dbs.find((db) => db.dbName === this.selectedDbName()) ?? null;
  });

  protected readonly queryLanguage = computed<QueryLanguage>(() =>
    resolveQueryLanguage(this.selectedDb()?.dbType),
  );
  protected readonly sqlDialect = computed<SQLDialect>(() =>
    resolveSqlDialect(this.selectedDb()?.dbType),
  );

  protected readonly canRun = computed(() => {
    if (!this.selectedService()) return false;
    if (this.availableDbs().length === 0) return false;
    if (this.availableDbs().length > 1 && !this.selectedDb()) return false;
    return this.queryText().trim().length > 0;
  });

  protected onServiceChange(name: string): void {
    this.selectedServiceName.set(name);
    this.selectedDbName.set(null);
    this.clearResults();
  }

  protected onDbChange(dbName: string): void {
    this.selectedDbName.set(dbName);
    this.clearResults();
  }

  protected run(): void {
    const service = this.selectedService();
    if (!service || !this.canRun()) {
      return;
    }

    this.isRunning.set(true);
    this.clearResults();

    const dbName = this.selectedDb()?.dbName ?? undefined;
    const query = this.queryText();
    const language = this.queryLanguage();

    const request$: Observable<SqlResult | MongoResult> =
      language === 'sql'
        ? this.dbConsole.executeSqlQuery(service.dockerService, query, dbName)
        : this.dbConsole.executeMongoQuery(service.dockerService, query, dbName);

    request$.subscribe({
      next: (result) => {
        this.isRunning.set(false);
        if (language === 'sql') {
          this.sqlResult.set(result as SqlResult);
        } else {
          this.mongoResult.set(result as MongoResult);
        }
      },
      error: (err: unknown) => {
        this.isRunning.set(false);
        this.runError.set(err instanceof Error ? err.message : 'Query failed.');
      },
    });
  }

  private clearResults(): void {
    this.sqlResult.set(null);
    this.mongoResult.set(null);
    this.runError.set(null);
  }
}
