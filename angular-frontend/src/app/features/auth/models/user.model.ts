export interface User {
  username: string;
  email: string;
  userId: string;
  roles: Role[];
}

export interface Role {
  name: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  address?: string;
  city?: string;
  province?: string;
  telephone?: string;
}

export interface TokenResponse {
  token: string;
  user: User;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface UserDetails {
  userId: string;
  username: string;
  email: string;
  roles: Role[];
}

export interface RoleUpdate {
  userId: string;
  roles: string[];
}

