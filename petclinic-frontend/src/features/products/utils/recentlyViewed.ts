// this code class was created to group the 'recently viewed' logic for
// individual products and bundles.
import type { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

import { getProduct } from '../api/getProduct';

const MAX_RECENT = 5;

function getStorageKey(userId?: string): string {
  return `recentlyClickedProducts_${userId || 'guest'}`;
}
export function getRecentlyViewed(userId?: string): ProductModel[] {
  try {
    const raw = localStorage.getItem(getStorageKey(userId));
    return raw ? (JSON.parse(raw) as ProductModel[]) : [];
  } catch {
    return [];
  }
}

export function addProductToRecentlyViewed(
  product: ProductModel,
  userId?: string
): void {
  try {
    const key = getStorageKey(userId);
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

// Local storage identifies history; the API supplies current bilingual content.
export async function loadRecentlyViewed(
  userId?: string
): Promise<ProductModel[]> {
  try {
    const saved: unknown = JSON.parse(
      localStorage.getItem(getStorageKey(userId)) || '[]'
    );
    if (!Array.isArray(saved)) return [];
    const ids = [
      ...new Set(
        saved.flatMap(item =>
          item && typeof item.productId === 'string' && item.productId.trim()
            ? [item.productId as string]
            : []
        )
      ),
    ].slice(0, MAX_RECENT);
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
