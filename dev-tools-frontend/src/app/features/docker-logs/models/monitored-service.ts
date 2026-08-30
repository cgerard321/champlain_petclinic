export interface ServiceDb {
  dbName: string | null;
  dbHost: string | null;
  dbType: string | null;
}

export interface MonitoredService {
  name: string;
  dockerService: string;
  dbs: ServiceDb[] | null;
}
