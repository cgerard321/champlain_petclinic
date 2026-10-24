export interface UserRole {
  id: number;
  name: string;
}

// One user as returned by GET /api/v2/gateway/users
export interface User {
  userId: string;
  username: string;
  email: string;
  roles: UserRole[];
}

// Body of PUT /api/v2/gateway/users/{id} (the backend only saves username and email)
export interface UserUpdateRequest {
  userId: string;
  username: string;
  email: string;
  roles: UserRole[];
}

// What the "Add user" form sends. The profile fields depend on the role:
// vets need the vet fields and owners need the owner fields.
export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  role: string;
  firstName?: string;
  lastName?: string;
  // vets only
  phoneNumber?: string;
  resume?: string;
  // owners only
  address?: string;
  city?: string;
  province?: string;
  telephone?: string;
}

// What the user changed in the "Edit user" form
export interface UserChanges {
  username: string;
  email: string;
  role: string;
}

export interface RoleOption {
  value: string;
  label: string;
}

// The roles an administrator can manage (values match the backend role names)
export const USER_ROLE_OPTIONS: readonly RoleOption[] = [
  { value: 'INVENTORY_MANAGER', label: 'Inventory Manager' },
  { value: 'VET', label: 'Vet' },
  { value: 'RECEPTIONIST', label: 'Receptionist' },
  { value: 'CUST', label: 'Owner' },
];

// The role name of a user (we only use the first role)
export function userRoleName(user: User): string {
  return user.roles[0]?.name ?? '';
}

// The text shown in the table for a role name
export function roleLabel(roleName: string): string {
  if (roleName === 'ADMIN') {
    return 'Administrator';
  }

  const option = USER_ROLE_OPTIONS.find((item) => item.value === roleName);

  return option ? option.label : roleName;
}

// Administrators are listed but cannot be edited from this page
export function isManagedRole(roleName: string): boolean {
  return USER_ROLE_OPTIONS.some((option) => option.value === roleName);
}
