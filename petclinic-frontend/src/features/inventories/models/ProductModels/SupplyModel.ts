export interface SupplyModel {
  supplyName: string;
  supplyDescription: string;
  supplyPrice: number;
  supplyQuantity: number;
  supplySalePrice: number;
  supplyStatus: 'RE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
}
