export interface Inventory {
  inventoryId: string;
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  important?: boolean;
}
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
