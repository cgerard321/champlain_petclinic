// this code class was created to group the 'recently viewed' logic for
// individual products and bundles.
import type { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

const MAX_RECENT = 5;

export function getRecentlyViewed(userId?: string): ProductModel[] {
  if (!userId) return [];
  try {
    const raw = localStorage.getItem(`recentlyClickedProducts_${userId}`);
    return raw ? (JSON.parse(raw) as ProductModel[]) : [];
  } catch {
    return [];
  }
}

export function addProductToRecentlyViewed(
  product: ProductModel,
  userId?: string
): void {
  if (!userId) return;
  try {
    const key = `recentlyClickedProducts_${userId}`;
    const raw = localStorage.getItem(key);
    const list: ProductModel[] = raw ? JSON.parse(raw) : [];
    const updated = list.filter(p => p.productId !== product.productId);
    updated.unshift(product);
    if (updated.length > MAX_RECENT) updated.splice(MAX_RECENT);
    localStorage.setItem(key, JSON.stringify(updated));
  } catch (e) {
    console.error('Could not update recently viewed', e);
  }
}
