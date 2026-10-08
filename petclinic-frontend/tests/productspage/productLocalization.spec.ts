import { test, expect } from '@playwright/test';

const baseURL = process.env.LOCALIZATION_BASE_URL || 'http://localhost:3000';
const product = {
  productId: 'french-product',
  imageId: '',
  productName: 'Cat Litter',
  productDescription: 'Clumping cat litter',
  productNameFr: 'Litière pour chats',
  productDescriptionFr: 'Litière agglomérante avec contrôle des odeurs',
  productSalePrice: 12.99,
  productQuantity: 10,
  averageRating: 0,
  requestCount: 0,
  productType: 'ACCESSORY',
  productStatus: 'AVAILABLE',
  isUnlisted: false,
  deliveryType: 'PICKUP',
};
const fallback = {
  ...product,
  productId: 'legacy-product',
  productName: 'Legacy Product',
  productDescription: 'Original description',
  productNameFr: null,
  productDescriptionFr: '   ',
};

test.beforeEach(async ({ page }) => {
  // Restrict mocks to gateway calls, leaving Vite source and translation requests intact.
  await page.route(/\/api\/(?:v2\/)?gateway\//, async route => {
    const path = new URL(route.request().url()).pathname;
    if (path.endsWith('/users/jwt')) {
      await route.fulfill({ status: 401, json: {} });
    } else if (path.endsWith('/products/bundles')) {
      await route.fulfill({
        json: [
          {
            bundleId: 'bundle',
            bundleName: 'Cat Bundle',
            bundleDescription: 'Bundle',
            productIds: [product.productId],
            originalTotalPrice: 12.99,
            bundlePrice: 10,
          },
        ],
      });
    } else if (path.endsWith('/products/types')) {
      await route.fulfill({ contentType: 'text/event-stream', body: '' });
    } else if (path.endsWith('/products')) {
      await route.fulfill({
        contentType: 'text/event-stream',
        body: [product, fallback]
          .map(p => `data:${JSON.stringify(p)}\n\n`)
          .join(''),
      });
    } else if (path.endsWith('/products/french-product')) {
      await route.fulfill({ json: product });
    } else if (path.endsWith('/products/legacy-product')) {
      await route.fulfill({ json: fallback });
    } else if (path.includes('/ratings/')) {
      await route.fulfill({ contentType: 'text/event-stream', body: '' });
    } else {
      await route.fulfill({ json: [] });
    }
  });
});

test('guest switches catalog and bundle product names between English and French', async ({
  page,
}) => {
  await page.goto(`${baseURL}/products`);
  await expect(
    page.getByRole('heading', { name: 'Welcome to PetClinic Shop Page!' })
  ).toBeVisible();
  const cards = page.locator('.product-card');
  await expect(
    cards.getByRole('heading', { name: 'Cat Litter' }).first()
  ).toBeVisible();
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('link', { name: 'Shop', exact: true })
  ).toBeVisible();
  await expect(
    cards.getByRole('heading', { name: 'Litière pour chats' }).first()
  ).toBeVisible();
  await expect(
    page
      .locator('.product-bundle-item')
      .getByText('Litière pour chats', { exact: true })
  ).toBeVisible();
  await expect(
    cards.getByRole('heading', { name: 'Legacy Product' }).first()
  ).toBeVisible();
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  await expect(
    cards.getByRole('heading', { name: 'Cat Litter' }).first()
  ).toBeVisible();
});

test('guest can search French names without typing accents', async ({
  page,
}) => {
  await page.goto(`${baseURL}/products`);
  await expect(
    page
      .locator('.product-card')
      .getByRole('heading', { name: 'Cat Litter' })
      .first()
  ).toBeVisible();
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('link', { name: 'Shop', exact: true })
  ).toBeVisible();
  await page.getByPlaceholder('Rechercher un produit…').fill('LITIERE');
  await expect(
    page
      .locator('.product-card')
      .getByRole('heading', { name: 'Litière pour chats' })
      .first()
  ).toBeVisible();
  // The catalog is filtered; trending/recent products remain independent sections.
  await expect(
    page
      .locator('.product-list-container')
      .first()
      .getByRole('heading', { name: 'Legacy Product' })
  ).toHaveCount(0);
});

test('direct details page switches both fields and falls back for legacy data', async ({
  page,
}) => {
  await page.goto(`${baseURL}/products/french-product`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('link', { name: 'Shop', exact: true })
  ).toBeVisible();
  await expect(
    page.getByText(product.productDescriptionFr, { exact: true })
  ).toBeVisible();
  await expect(
    page.getByRole('heading', { name: product.productNameFr })
  ).toBeVisible();
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  await expect(
    page.getByText(product.productDescription, { exact: true })
  ).toBeVisible();
  await page.goto(`${baseURL}/products/legacy-product`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('link', { name: 'Shop', exact: true })
  ).toBeVisible();
  await expect(
    page.getByText(fallback.productDescription, { exact: true })
  ).toBeVisible();
});

test('French filters and sorting preserve API values and switch back to English', async ({
  page,
}) => {
  await page.goto(`${baseURL}/products`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('heading', {
      name: 'Bienvenue dans la boutique PetClinic !',
    })
  ).toBeVisible();
  await page.getByRole('button', { name: '☰ Filtres', exact: true }).click();
  await page.getByLabel('Mode de livraison :').selectOption('PICKUP');
  const request = page.waitForRequest(
    r =>
      r.url().includes('/products?') && r.url().includes('deliveryType=PICKUP')
  );
  await page.getByRole('button', { name: 'Appliquer', exact: true }).click();
  await request;
  await page.getByRole('button', { name: 'Trier par', exact: true }).click();
  await page.getByRole('menuitem', { name: 'Prix : croissant' }).click();
  await expect(
    page
      .locator('.product-card')
      .first()
      .getByRole('button', { name: 'Ajouter au panier' })
  ).toBeVisible();
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  await expect(
    page.getByRole('button', { name: 'Sort By', exact: true })
  ).toBeVisible();
});

test('shop labels work even when the browser has stale empty locale responses', async ({
  page,
}) => {
  await page.route('**/locales/*/products.json', route =>
    route.fulfill({ json: {} })
  );
  await page.goto(`${baseURL}/products`);
  await expect(
    page.getByRole('heading', { name: 'Welcome to PetClinic Shop Page!' })
  ).toBeVisible();
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('heading', {
      name: 'Bienvenue dans la boutique PetClinic !',
    })
  ).toBeVisible();
  await expect(
    page.getByRole('heading', { name: 'Catalogue', exact: true })
  ).toBeVisible();
  await expect(
    page
      .locator('.product-card')
      .first()
      .getByRole('button', { name: 'Ajouter au panier' })
  ).toBeVisible();
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  await expect(
    page.getByRole('heading', { name: 'Welcome to PetClinic Shop Page!' })
  ).toBeVisible();
});
