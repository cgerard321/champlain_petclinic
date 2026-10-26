// this code class was created to group the 'recently viewed' logic for
// individual products and bundles.
import type { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

import { getProduct } from '../api/getProduct';

const MAX_RECENT = 5;

interface RecentlyViewedEntry {
  productId: string;
}

function isRecentlyViewedEntry(value: unknown): value is RecentlyViewedEntry {
  return (
    typeof value === 'object' &&
    value !== null &&
    'productId' in value &&
    typeof value.productId === 'string' &&
    value.productId.trim().length > 0
  );
}

export function getRecentlyViewed(userId?: string): RecentlyViewedEntry[] {
  if (!userId) return [];
  try {
    const raw = localStorage.getItem(`recentlyClickedProducts_${userId}`);
    if (!raw) return [];

    const saved: unknown = JSON.parse(raw);
    if (!Array.isArray(saved)) return [];

    // Older entries may contain the full product. Keeping only productId here
    // makes that data compatible with the new, smaller storage format.
    return saved
      .filter(isRecentlyViewedEntry)
      .map(({ productId }) => ({ productId }));
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
    const list = getRecentlyViewed(userId);
    const updated = list.filter(p => p.productId !== product.productId);
    updated.unshift({ productId: product.productId });
    localStorage.setItem(key, JSON.stringify(updated.slice(0, MAX_RECENT)));
  } catch (e) {
    console.error('Could not update recently viewed', e);
  }
}

// Local storage identifies history; the API supplies current bilingual content.
export async function loadRecentlyViewed(
  userId?: string
): Promise<ProductModel[]> {
  if (!userId) return [];
  try {
    const saved = getRecentlyViewed(userId);
    const ids = [...new Set(saved.map(item => item.productId))].slice(
      0,
      MAX_RECENT
    );
    const products = await Promise.all(
      ids.map(id => getProduct(id, true).catch(() => null))
    );
    return products.filter(
      (p): p is ProductModel => !!p && !!p.productId && !p.isUnlisted
    );
  } catch {
    return [];
  }
}
