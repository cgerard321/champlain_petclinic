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

test('search matches translated types without accents and follows language changes', async ({
  page,
}) => {
  await page.route('**/api/gateway/products?*', route =>
    route.fulfill({
      contentType: 'text/event-stream',
      body: `data:${JSON.stringify({ ...product, productType: 'EQUIPMENT' })}\n\n`,
    })
  );
  await page.goto(`${baseURL}/products`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await page.getByPlaceholder('Rechercher un produit…').fill('equipement');
  const catalog = page.locator('.product-list-container').first();
  await expect(
    catalog.getByRole('heading', { name: product.productNameFr })
  ).toBeVisible();
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  await expect(catalog.locator('.product-card')).toHaveCount(0);
  await page.locator('input[type="text"]').first().fill('equipment');
  await expect(
    catalog.getByRole('heading', { name: product.productName })
  ).toBeVisible();
});

test('recent history fetches bilingual data independently of the catalog and skips unavailable products', async ({
  page,
}) => {
  await page.addInitScript(() =>
    localStorage.setItem(
      'recentlyClickedProducts_review-user',
      JSON.stringify([
        {
          productId: 'history-product',
          productName: 'Old cached English name',
        },
        { productId: 'deleted-product', productName: 'Deleted product' },
        { productId: 'hidden-product', productName: 'Hidden product' },
        { productId: 'failed-product', productName: 'Failed product' },
      ])
    )
  );
  await page.route('**/api/gateway/users/jwt', route =>
    route.fulfill({
      json: {
        userId: 'review-user',
        username: 'Reviewer',
        email: 'review@example.com',
        roles: ['CUST'],
      },
    })
  );
  let historyReads = 0;
  let historyWrites = 0;
  await page.route('**/api/gateway/products/history-product?*', route => {
    if (route.request().method() === 'GET') historyReads++;
    else historyWrites++;
    return route.fulfill({
      json: {
        ...product,
        productId: 'history-product',
        productNameFr: 'Produit récemment consulté',
      },
    });
  });
  await page.route('**/api/gateway/products/deleted-product?*', route =>
    route.fulfill({ status: 404, json: {} })
  );
  await page.route('**/api/gateway/products/failed-product?*', route =>
    route.fulfill({ status: 500, json: {} })
  );
  await page.route('**/api/gateway/products/hidden-product?*', route =>
    route.fulfill({ json: { ...product, isUnlisted: true } })
  );
  await page.goto(`${baseURL}/products`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  const recent = page.locator('.recently-viewed-flex');
  await expect(async () => {
    await recent.scrollIntoViewIfNeeded();
  }).toPass();
  await expect(
    recent.getByRole('heading', { name: 'Produit récemment consulté' })
  ).toBeVisible();
  await expect(recent.locator('.product-card')).toHaveCount(1);
  expect(historyReads).toBeGreaterThan(0);
  expect(historyWrites).toBe(0);
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  await expect(async () => {
    await recent.scrollIntoViewIfNeeded();
  }).toPass();
  await expect(
    recent.getByRole('heading', { name: product.productName })
  ).toBeVisible();
  // The details page uses the separate recently-viewed component.
  await page.goto(`${baseURL}/products/legacy-product`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(async () => {
    await recent.scrollIntoViewIfNeeded();
  }).toPass();
  await expect(
    recent.getByRole('heading', { name: 'Produit récemment consulté' })
  ).toBeVisible();
});

test('merged Markdown reviews keep French labels, formatting and validation', async ({
  page,
}) => {
  await page.route('**/api/gateway/users/jwt', route =>
    route.fulfill({
      json: {
        userId: 'reader',
        username: 'Reader',
        email: 'reader@example.com',
        roles: ['CUST'],
      },
    })
  );
  await page.route('**/api/gateway/ratings/french-product', route =>
    route.fulfill({ json: { rating: 0, review: '' } })
  );
  await page.route('**/api/gateway/ratings/product/french-product', route =>
    route.fulfill({
      contentType: 'text/event-stream',
      body: `data:${JSON.stringify({ rating: 5, review: '**Excellent product**', customerId: 'other', reviewerUsername: 'Reviewer' })}\n\n`,
    })
  );
  await page.goto(`${baseURL}/products/french-product`);
  await page.getByRole('button', { name: 'FR', exact: true }).click();
  await expect(
    page.getByRole('heading', { name: 'Avis des clients', exact: true })
  ).toBeVisible();
  await expect(page.locator('.reviewsforproduct-container strong')).toHaveText(
    'Excellent product'
  );
  await page.getByPlaceholder('Rechercher un avis…').fill('Excellent');
  await expect(page.locator('.reviews-search-result-count')).toHaveText(
    '1 avis affiché(s) sur 1'
  );
  await page
    .getByRole('button', { name: 'Rédiger un avis', exact: true })
    .click();
  const dialog = page.getByRole('dialog');
  await expect(
    dialog.getByText('Masquer l’aperçu', { exact: true })
  ).toBeVisible();
  await expect(
    dialog.getByRole('button', {
      name: 'Mettre la sélection en gras',
      exact: true,
    })
  ).toBeVisible();
  const input = dialog.locator('textarea');
  await input.fill('**Mon avis**');
  await expect(dialog.locator('.reviewbox-preview-body strong')).toHaveText(
    'Mon avis'
  );
  await input.fill('a'.repeat(10001));
  await expect(dialog.getByRole('alert')).toHaveText(
    'L’avis ne peut pas dépasser 10 000 caractères !'
  );
});
