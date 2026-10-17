import { TFunction } from 'i18next';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { localizedProduct, normalizeProductSearch } from './localizedProduct';

export type AvailabilityFilter = 'in-stock' | 'pre-order' | 'out-of-stock';

export type ProductFilters = {
  minPrice?: number;
  maxPrice?: number;
  ratingSort?: string;
  minStars?: number;
  maxStars?: number;
  deliveryType?: string;
  productType?: string;
  availability?: string;
};

export type FilterKey = 'productType' | 'deliveryType' | 'availability';

export type FilterMatchOptions = {
  query?: string;
  language?: string;
  t?: TFunction;
};

export const matchesAvailability = (
  product: ProductModel,
  value: AvailabilityFilter
): boolean => {
  const isInStock =
    product.productQuantity > 0 && product.productStatus !== 'OUT_OF_STOCK';

  if (value === 'in-stock') return isInStock;
  if (value === 'pre-order') return product.productStatus === 'PRE_ORDER';
  return !isInStock;
};

export const matchesTypes = (
  product: ProductModel,
  selectedTypes: string[]
): boolean =>
  selectedTypes.length === 0 || selectedTypes.includes(product.productType);

export const matchesDelivery = (
  product: ProductModel,
  deliveryType?: string
): boolean =>
  !deliveryType ||
  String(product.deliveryType).toUpperCase() === deliveryType.toUpperCase();

export const matchesPrice = (
  product: ProductModel,
  minPrice?: number,
  maxPrice?: number
): boolean =>
  (minPrice === undefined || product.productSalePrice >= minPrice) &&
  (maxPrice === undefined || product.productSalePrice <= maxPrice);

export const matchesRating = (
  product: ProductModel,
  minStars = 0,
  maxStars = 5
): boolean =>
  product.averageRating >= minStars && product.averageRating <= maxStars;

export const matchesSearch = (
  product: ProductModel,
  query: string,
  language: string,
  t: TFunction
): boolean => {
  const normalizedQuery = normalizeProductSearch(query);
  if (normalizedQuery === '') return true;

  return (
    normalizeProductSearch(localizedProduct(product, language).name).includes(
      normalizedQuery
    ) ||
    normalizeProductSearch(
      t(`types.${(product.productType || '').toUpperCase()}`, {
        defaultValue: product.productType || '',
      })
    ).includes(normalizedQuery)
  );
};

export const matchesFilters = (
  product: ProductModel,
  filters: ProductFilters,
  options: FilterMatchOptions = {}
): boolean => {
  const selectedTypes = filters.productType
    ? filters.productType.split(',').filter(Boolean)
    : [];
  const selectedAvailability = filters.availability
    ? filters.availability.split(',').filter(Boolean)
    : [];

  return (
    matchesTypes(product, selectedTypes) &&
    matchesDelivery(product, filters.deliveryType) &&
    matchesPrice(product, filters.minPrice, filters.maxPrice) &&
    matchesRating(product, filters.minStars ?? 0, filters.maxStars ?? 5) &&
    (selectedAvailability.length === 0 ||
      selectedAvailability.some(value =>
        matchesAvailability(product, value as AvailabilityFilter)
      )) &&
    (options.query === undefined ||
      !options.t ||
      matchesSearch(
        product,
        options.query,
        options.language ?? 'en',
        options.t
      ))
  );
};
