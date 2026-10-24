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
// vets need the vet fields and customers need the customer fields.
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
  // customers only
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

// The roles an administrator can manage (these are the backend role names)
export const MANAGED_ROLE_NAMES = ['INVENTORY_MANAGER', 'VET', 'RECEPTIONIST', 'CUST'];

// The text shown for a role name. It is a function, and not a constant, because the
// translations are only installed when the app starts.
export function roleLabel(roleName: string): string {
  switch (roleName) {
    case 'ADMIN':
      return $localize`:@@role.admin:Administrateur`;
    case 'VET':
      return $localize`:@@role.vet:Vétérinaire`;
    case 'RECEPTIONIST':
      return $localize`:@@role.receptionist:Réceptionniste`;
    case 'INVENTORY_MANAGER':
      return $localize`:@@role.inventoryManager:Gestionnaire d'inventaire`;
    case 'CUST':
      return $localize`:@@role.customer:Client`;
    default:
      return roleName;
  }
}

// The roles to choose from in the forms
export function userRoleOptions(): RoleOption[] {
  return MANAGED_ROLE_NAMES.map((name) => ({ value: name, label: roleLabel(name) }));
}

// The role name of a user (we only use the first role)
export function userRoleName(user: User): string {
  return user.roles[0]?.name ?? '';
}

// Administrators are listed but cannot be edited or deleted from this page
export function isManagedRole(roleName: string): boolean {
  return MANAGED_ROLE_NAMES.includes(roleName);
}
