export interface InventoryResponseModel {
  inventoryId: string;
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  inventoryImage: string;
  inventoryBackupImage: string;
  imageUploaded: Uint8Array | null;
}
