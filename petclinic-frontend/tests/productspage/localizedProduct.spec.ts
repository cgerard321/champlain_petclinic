import { test, expect } from '@playwright/test';
import {
  localizedProduct,
  normalizeProductSearch,
} from '../../src/features/products/utils/localizedProduct';

const product = {
  productName: 'Cat Litter',
  productDescription: 'Clumping litter',
  productNameFr: 'Litière pour chats',
  productDescriptionFr: 'Contrôle des odeurs',
};

test('selects French and regional French without changing English data', () => {
  for (const language of ['fr', 'fr-CA', 'fr-FR']) {
    expect(localizedProduct(product, language)).toEqual({
      name: 'Litière pour chats',
      description: 'Contrôle des odeurs',
    });
  }
  expect(localizedProduct(product, 'en')).toEqual({
    name: 'Cat Litter',
    description: 'Clumping litter',
  });
  expect(product.productName).toBe('Cat Litter');
});

test('falls back independently for absent or blank French fields', () => {
  for (const missing of [undefined, null, '', '   ']) {
    expect(
      localizedProduct({ ...product, productNameFr: missing }, 'fr')
    ).toEqual({ name: 'Cat Litter', description: 'Contrôle des odeurs' });
    expect(
      localizedProduct({ ...product, productDescriptionFr: missing }, 'fr')
    ).toEqual({ name: 'Litière pour chats', description: 'Clumping litter' });
  }
});

test('search matches French accents and case with or without typed accents', () => {
  const name = normalizeProductSearch(localizedProduct(product, 'fr').name);
  expect(name).toContain(normalizeProductSearch('  LITIERE  '));
  expect(name).toContain(normalizeProductSearch('LITIÈRE'));
  expect(name).not.toContain(normalizeProductSearch('dog'));
});
