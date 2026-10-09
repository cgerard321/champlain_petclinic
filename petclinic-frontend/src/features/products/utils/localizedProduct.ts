import { ProductModel } from '../models/ProductModels/ProductModel';

type ProductText = Pick<
  ProductModel,
  | 'productName'
  | 'productDescription'
  | 'productNameFr'
  | 'productDescriptionFr'
>;

// Product content comes from the API, not the static UI translation dictionary.
export function localizedProduct(
  product: ProductText,
  language: string
): { name: string; description: string } {
  const french = language.toLowerCase().split('-')[0] === 'fr';
  return {
    name: (french && product.productNameFr?.trim()) || product.productName,
    description:
      (french && product.productDescriptionFr?.trim()) ||
      product.productDescription,
  };
}

export function normalizeProductSearch(text: string): string {
  return text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

export function formatProductPrice(amount: number, language: string): string {
  return new Intl.NumberFormat(language.startsWith('fr') ? 'fr-CA' : 'en-CA', {
    style: 'currency',
    currency: 'CAD',
  }).format(amount);
}
