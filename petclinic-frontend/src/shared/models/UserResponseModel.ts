import { Role } from '@/shared/models/Role.ts';

export interface UserResponseModel {
  username: string;
  email: string;
  userId: string;
  roles: Set<Role>;
  practitionerId?: string;
}
