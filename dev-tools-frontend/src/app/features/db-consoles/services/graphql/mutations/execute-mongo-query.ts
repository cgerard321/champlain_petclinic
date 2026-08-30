import { gql } from 'apollo-angular';

export const EXECUTE_MONGO_QUERY_MUTATION = gql`
  mutation ExecuteMongoQuery($service: String!, $mongoQuery: String!, $dbName: String) {
    executeMongoQuery(service: $service, mongoQuery: $mongoQuery, dbName: $dbName) {
      bson
      affectedCount
    }
  }
`;
