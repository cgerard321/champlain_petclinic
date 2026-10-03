import { Status } from '@features/supplies/models/status';

export interface Supply {
  productId: string;
  inventoryId: string;
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  status: Status;
  lastUpdatedAt: string;
  photoData?: string | null;
  photoType?: string | null;
}
