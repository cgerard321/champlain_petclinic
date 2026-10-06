import { Roles } from '@shared/models/roles';

/**
 * Capability map for inventory CRUD, one entry per role.
 *
 *
 * - ADMIN / INVENTORY_MANAGER: full CRUD on inventories and products,
 *   no divergence between the two roles anywhere.
 * - VET: can list inventories (`GET ""`) and list products in an
 *   inventory (`GET /{id}/products`), and can consume a product —
 *   but CANNOT fetch a single inventory by ID (`GET /{id}`) or a
 *   single product by ID (`GET /{id}/products/{pid}`). Those two
 *   single-item endpoints only allow ADMIN/INVENTORY_MANAGER, even
 *   though their list level equivalents allow VET. So a Vet-facing
 *   UI must get inventory/product details from the list response,
 *   not from a fresh single item fetch.
 * - RECEPTIONIST: not in any allowedRoles list. No access.
 */
export interface InventoryPermissions {
  hasAnyAccess: boolean;

  // List level reads
  canListInventories: boolean;
  canListProducts: boolean;

  // Single item reads. NOTE: false for Vet, even though list reads are true
  canViewInventoryDetail: boolean;
  canViewProductDetail: boolean;

  // Inventory CRUD
  canCreateInventory: boolean;
  canUpdateInventory: boolean;
  canDeleteInventory: boolean;

  // Product CRUD
  canAddProduct: boolean;
  canUpdateProduct: boolean;
  canDeleteProduct: boolean;

  // Not strictly CRUD, but tied to the same product level access rule
  canConsumeProduct: boolean;
  /*
   * Not CRUD, but a real endpoint (GET .../productquantity, ADMIN/INVENTORY_MANAGER
   * only) that the UI must hide for other roles otherwise the "hidden or disabled
   * in the UI" criterion isn't met even though the backend still 403s it.
   */
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
  canViewInventoryDetail: false, // GET /{id} excludes VET
  canViewProductDetail: false, // GET /{id}/products/{pid} excludes VET
  canCreateInventory: false,
  canUpdateInventory: false,
  canDeleteInventory: false,
  canAddProduct: false,
  canUpdateProduct: false,
  canDeleteProduct: false,
  canConsumeProduct: true,
  canViewProductQuantity: true, // GET .../productquantity excludes VET
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
  // Receptionist (for now): no live gateway access today.
  return NO_ACCESS;
}
