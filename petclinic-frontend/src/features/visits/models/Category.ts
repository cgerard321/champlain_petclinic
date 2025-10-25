import { Visit } from './Visit';

export interface Category {
  name: string;
  emergency?: boolean;
  list: Visit[];
}
