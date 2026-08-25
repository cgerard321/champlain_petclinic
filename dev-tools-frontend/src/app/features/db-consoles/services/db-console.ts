import { Injectable, inject } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable, map } from 'rxjs';
import { MongoResult } from '@features/db-consoles/models/mongo-result';
import { EXECUTE_MONGO_QUERY_MUTATION } from '@features/db-consoles/services/graphql/mutations/execute-mongo-query';
import { SqlResult } from '@features/db-consoles/models/sql-result';
import { EXECUTE_SQL_QUERY_MUTATION } from '@features/db-consoles/services/graphql/mutations/execute-sql-query';

interface ExecuteSqlQueryResponse {
  executeSqlQuery: SqlResult;
}

interface ExecuteMongoQueryResponse {
  executeMongoQuery: MongoResult;
}

@Injectable({ providedIn: 'root' })
export class DbConsole {
  private readonly apollo = inject(Apollo);

  executeSqlQuery(service: string, sql: string, dbName?: string): Observable<SqlResult> {
    return this.apollo
      .mutate<ExecuteSqlQueryResponse>({
        mutation: EXECUTE_SQL_QUERY_MUTATION,
        variables: { service, sql, dbName },
        fetchPolicy: 'no-cache',
      })
      .pipe(map((result) => this.unwrap(result.data?.executeSqlQuery)));
  }

  executeMongoQuery(service: string, mongoQuery: string, dbName?: string): Observable<MongoResult> {
    return this.apollo
      .mutate<ExecuteMongoQueryResponse>({
        mutation: EXECUTE_MONGO_QUERY_MUTATION,
        variables: { service, mongoQuery, dbName },
        fetchPolicy: 'no-cache',
      })
      .pipe(map((result) => this.unwrap(result.data?.executeMongoQuery)));
  }

  private unwrap<T>(value: T | undefined): T {
    if (!value) {
      throw new Error('Query returned no data.');
    }
    return value;
  }
}
