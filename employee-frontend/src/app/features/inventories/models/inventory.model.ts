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
