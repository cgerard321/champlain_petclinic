export interface CreateUserRequest {
  email: string;
  password: string;
  display_name: string;
  roles: string[];
}
