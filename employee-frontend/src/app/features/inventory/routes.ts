import { Routes } from '@angular/router';
import { InventoryList } from '@features/inventory/pages/inventory-list/inventory-list';
import { SupplyPage } from '@features/inventory/pages/supply-page/supply-page';

export default [
  // Create the paths to the inventory-list and the supply page
  { path: '', component: InventoryList },
  { path: ':inventoryId/supplies', component: SupplyPage },
] satisfies Routes;
