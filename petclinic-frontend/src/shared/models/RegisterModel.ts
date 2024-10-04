import { OwnerRequestModel } from './OwnerRequestModel';

export interface Register {
  userId: string;
  email: string;
  username: string;
  password: string;
  defaultRole?: string;
  owner: OwnerRequestModel;
}
