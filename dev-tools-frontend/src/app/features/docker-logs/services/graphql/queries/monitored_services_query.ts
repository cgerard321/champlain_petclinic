import { gql } from 'apollo-angular';

export const MONITORED_SERVICES_QUERY = gql`
  query MonitoredServices {
    queryMonitoredServices {
      name
      dockerService
      dbs {
        dbName
        dbHost
        dbType
      }
    }
  }
`;
