// Mirrors shared::config's role UUIDs on the Rust backend exactly.
// Keep in sync if those ever change
export const RoleId = {
  Sudo: '11111111-1111-1111-1111-111111111111',
  Admin: 'a48d7b18-ceb7-435b-b8ff-b28531f1a09f',
  Reader: '51f20832-79a3-4c05-b4da-ca175cba2ffc',
  Editor: '96ee5d72-c27b-4256-8db8-cf49d64e65de',
  AuthServiceDev: '7bbec248-56bd-4cde-bbc8-0f66d2155130',
  VetServiceDev: 'a99390f2-de4d-4616-88e9-9f7d4b8cdc7b',
  VisitsServiceDev: 'd30cfb07-2933-44d6-8035-924f850fc2ef',
  CustomersServiceDev: '208aea10-e4d6-4caf-82aa-616feaee869e',
  ProductsServiceDev: '84ab3608-e5d4-4e45-8b02-d1a38ae2cb9d',
  CartServiceDev: 'a51e4699-3250-4cf7-ac2a-836675f62175',
  InventoryServiceDev: 'a7975ad7-f86e-49b7-898d-95da0175f4e5',
  BillingServiceDev: 'd1d928c3-b8a8-4a01-8eaf-9f7dc1cf9739',
} as const;

export type RoleId = (typeof RoleId)[keyof typeof RoleId];
