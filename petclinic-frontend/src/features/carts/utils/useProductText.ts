import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getProduct } from '@/features/products/api/getProduct';
import { localizedProduct } from '@/features/products/utils/localizedProduct';
import type { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

type CartProduct = {
  productId?: string | number;
  productName: string;
  productDescription?: string;
};

// products loaded from products api
const loadedProducts = new Map<string, ProductModel>();

// product ids we already asked the api
const requestedIds = new Set<string>();

const refreshCallbacks = new Set<() => void>();

// using the translation from product api
export function useProductText(products: CartProduct[]): {
  localizedName: (product: CartProduct) => string;
  localizedDescription: (product: CartProduct) => string;
} {
  const { i18n } = useTranslation('carts');
  const [, setRefresh] = useState(0);

  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const isFrench = language.toLowerCase().startsWith('fr');

  const ids: string[] = [];
  for (const product of products) {
    const id = String(product.productId ?? '').trim();
    if (id !== '' && !ids.includes(id)) {
      ids.push(id);
    }
  }
  const idsKey = ids.sort().join('|');

  useEffect(() => {
    const refresh = (): void => setRefresh(count => count + 1);
    refreshCallbacks.add(refresh);

    return () => {
      refreshCallbacks.delete(refresh);
    };
  }, []);

  useEffect(() => {
    if (!isFrench || idsKey === '') {
      return;
    }

    for (const id of idsKey.split('|')) {
      if (requestedIds.has(id)) {
        continue;
      }
      requestedIds.add(id);

      getProduct(id, true)
        .then(product => {
          loadedProducts.set(id, product);
          refreshCallbacks.forEach(refresh => refresh());
        })
        .catch(() => {
          requestedIds.delete(id);
        });
    }
  }, [isFrench, idsKey]);

  // falls back to cart
  const getText = (
    product: CartProduct
  ): { name: string; description: string } => {
    const loaded = loadedProducts.get(String(product.productId));

    if (isFrench && loaded) {
      return localizedProduct(loaded, language);
    }

    return {
      name: product.productName,
      description: product.productDescription ?? '',
    };
  };

  const localizedName = (product: CartProduct): string => {
    return getText(product).name;
  };

  const localizedDescription = (product: CartProduct): string => {
    return getText(product).description;
  };

  return { localizedName, localizedDescription };
}
