import { RoleId } from '@shared/models/roles';

export interface AssignableRole {
  id: string;
  label: string;
}

// SUDO is excluded on purpose
export const ASSIGNABLE_ROLES: AssignableRole[] = [
  { id: RoleId.Admin, label: 'Admin' },
  { id: RoleId.Editor, label: 'Editor' },
  { id: RoleId.Reader, label: 'Reader' },
  { id: RoleId.AuthServiceDev, label: 'Auth Service Dev' },
  { id: RoleId.VetServiceDev, label: 'Vet Service Dev' },
  { id: RoleId.VisitsServiceDev, label: 'Visits Service Dev' },
  { id: RoleId.CustomersServiceDev, label: 'Customers Service Dev' },
  { id: RoleId.ProductsServiceDev, label: 'Products Service Dev' },
  { id: RoleId.CartServiceDev, label: 'Cart Service Dev' },
  { id: RoleId.InventoryServiceDev, label: 'Inventory Service Dev' },
  { id: RoleId.BillingServiceDev, label: 'Billing Service Dev' },
];
