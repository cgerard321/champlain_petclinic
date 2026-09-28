import { CustomerRequestModel } from './CustomerRequestModel.ts';

export interface Register {
  userId: string;
  email: string;
  username: string;
  password: string;
  defaultRole?: string;
  owner: CustomerRequestModel;
}
