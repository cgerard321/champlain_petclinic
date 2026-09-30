export interface Inventory {
  inventoryId: string;
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  important?: boolean;
}

export interface InventoryRequest {
  inventoryName: string;        
  inventoryType: string;       
  inventoryDescription: string; 
}


export const INVENTORY_TYPES = ['Bandages', 'Injections', 'Medications', 'Equipment'] as const;
export type InventoryType = (typeof INVENTORY_TYPES)[number];