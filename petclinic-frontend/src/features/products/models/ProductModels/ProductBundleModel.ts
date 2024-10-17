export interface ProductBundleModel {
  bundleId: string;
  bundleName: string;
  bundleDescription: string;
  productIds: string[];
  originalTotalPrice: number;
  bundlePrice: number;
}
