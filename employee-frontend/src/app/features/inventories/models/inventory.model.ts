export interface Inventory {
  inventoryId: string;
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  name?: { en: string; fr: string };
  description?: { en: string; fr: string };
  important?: boolean;
}

export interface InventoryRequest {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  name?: { en: string; fr: string };
  description?: { en: string; fr: string };
}

export const INVENTORY_TYPES = ['Bandages', 'Injections', 'Medications', 'Equipment'] as const;
export type InventoryTypeValue = (typeof INVENTORY_TYPES)[number];

export interface InventoryFilters {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  importantOnly: boolean;
}
export interface InventoryType {
  typeId: string;
  type: string;
}
