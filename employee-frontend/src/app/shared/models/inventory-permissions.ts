import { Roles } from '@shared/models/roles';


export interface InventoryPermissions {
  hasAnyAccess: boolean;

  canListInventories: boolean;
  canListProducts: boolean;

  canViewInventoryDetail: boolean;
  canViewProductDetail: boolean;

  canCreateInventory: boolean;
  canUpdateInventory: boolean;
  canDeleteInventory: boolean;

  canAddProduct: boolean;
  canUpdateProduct: boolean;
  canDeleteProduct: boolean;

  canConsumeProduct: boolean;
  canViewProductQuantity: boolean;
}

const FULL_ACCESS: InventoryPermissions = {
  hasAnyAccess: true,
  canListInventories: true,
  canListProducts: true,
  canViewInventoryDetail: true,
  canViewProductDetail: true,
  canCreateInventory: true,
  canUpdateInventory: true,
  canDeleteInventory: true,
  canAddProduct: true,
  canUpdateProduct: true,
  canDeleteProduct: true,
  canConsumeProduct: true,
  canViewProductQuantity: true,
};

const VET_ACCESS: InventoryPermissions = {
  hasAnyAccess: true,
  canListInventories: true,
  canListProducts: true,
  canViewInventoryDetail: false,
  canViewProductDetail: false,
  canCreateInventory: false,
  canUpdateInventory: false,
  canDeleteInventory: false,
  canAddProduct: false,
  canUpdateProduct: false,
  canDeleteProduct: false,
  canConsumeProduct: true,
  canViewProductQuantity: true,
};

const NO_ACCESS: InventoryPermissions = {
  hasAnyAccess: false,
  canListInventories: false,
  canListProducts: false,
  canViewInventoryDetail: false,
  canViewProductDetail: false,
  canCreateInventory: false,
  canUpdateInventory: false,
  canDeleteInventory: false,
  canAddProduct: false,
  canUpdateProduct: false,
  canDeleteProduct: false,
  canConsumeProduct: false,
  canViewProductQuantity: false,
};

export function getInventoryPermissions(roles: readonly string[]): InventoryPermissions {
  if (roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager)) {
    return FULL_ACCESS;
  }
  if (roles.includes(Roles.vet)) {
    return VET_ACCESS;
  }
  return NO_ACCESS;
}
