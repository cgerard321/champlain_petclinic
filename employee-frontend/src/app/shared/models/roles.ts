export const Roles = {
  admin: 'ADMIN',
  owner: 'OWNER',
  receptionist: 'RECEPTIONIST',
  vet: 'VET',
  inventoryManager: 'INVENTORY_MANAGER',
} as const;

export type Roles = (typeof Roles)[keyof typeof Roles];

export const CUSTOMER_ROLES: Roles[] = [Roles.owner];
export const EMPLOYEE_ROLES: Roles[] = [
  Roles.admin,
  Roles.vet,
  Roles.receptionist,
  Roles.inventoryManager,
];
