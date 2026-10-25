export interface Bundle {
  bundleId: string;
  bundleName: string;
  bundleDescription: string;
  productIds: string[];
  originalTotalPrice: number;
  bundlePrice: number;
}

export interface BundleRequest {
  bundleName: string;
  bundleDescription: string;
  productIds: string[];
  bundlePrice: number;
}
