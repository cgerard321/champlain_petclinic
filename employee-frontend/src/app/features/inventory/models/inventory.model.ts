export interface Inventory {
  // Same fields as the ones in InventoryResponseDTO
  inventoryId: string;
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  important?: boolean;
}
