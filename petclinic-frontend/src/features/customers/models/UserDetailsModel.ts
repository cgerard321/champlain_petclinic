export interface UserDetailsModel {
  userId: string;
  username: string;
  email: string;
  roles: string[];
  verified: boolean;
  disabled: boolean;
}
