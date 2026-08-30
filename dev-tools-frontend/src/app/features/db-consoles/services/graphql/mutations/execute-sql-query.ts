import { gql } from 'apollo-angular';

export const EXECUTE_SQL_QUERY_MUTATION = gql`
  mutation ExecuteSqlQuery($service: String!, $sql: String!, $dbName: String) {
    executeSqlQuery(service: $service, sql: $sql, dbName: $dbName) {
      columns
      rows
      affectedRows
    }
  }
`;
