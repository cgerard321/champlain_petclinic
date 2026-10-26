import { test, expect } from '@playwright/test';

const vets = [
  {
    vetId: '11111111-1111-1111-1111-111111111111',
    firstName: 'Alex',
    lastName: 'Morin',
    active: true,
    specialties: [{ specialtyId: 'surgery', name: 'Surgery' }],
  },
  {
    vetId: '22222222-2222-2222-2222-222222222222',
    firstName: 'Julie',
    lastName: 'Lambert',
    active: true,
    specialties: [],
  },
];

test.describe('Veterinarian directory response contract', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/**', async route => {
      await route.fulfill({ status: 200, json: [] });
    });
    await page.route('**/api/gateway/users/jwt', async route => {
      await route.fulfill({
        status: 200,
        json: {
          username: 'admin',
          userId: 'test-admin',
          email: 'admin@example.com',
          roles: ['ADMIN'],
        },
      });
    });
  });

  for (const status of [200, 201]) {
    test(`directory renders separate profiles for an SSE ${status} response`, async ({
      page,
    }) => {
      await page.route('**/api/v2/gateway/vets', async route => {
        // Model content negotiation: a missing SSE header reproduces the blank-card bug.
        const wantsStream =
          route.request().headers()['accept'] === 'text/event-stream';
        await route.fulfill({
          status,
          contentType: wantsStream ? 'text/event-stream' : 'application/json',
          body: wantsStream
            ? vets.map(vet => `data:${JSON.stringify(vet)}\n\n`).join('')
            : JSON.stringify(vets),
        });
      });
      const request = page.waitForRequest('**/api/v2/gateway/vets');
      await page.goto('http://localhost:3000/vets');
      expect((await request).headers()['accept']).toBe('text/event-stream');
      await expect(page.locator('.card-vets')).toHaveCount(2);
      await expect(
        page.getByRole('heading', { name: 'Alex Morin' })
      ).toBeVisible();
      await expect(
        page.getByRole('heading', { name: 'Julie Lambert' })
      ).toBeVisible();
      await expect(page.getByText('Error fetching all vets.')).toHaveCount(0);
      await expect(page.getByText('No results found.')).toHaveCount(0);
    });
  }

  test('successful empty stream displays the empty state', async ({ page }) => {
    await page.route('**/api/v2/gateway/vets', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'text/event-stream',
        body: '',
      });
    });
    await page.goto('http://localhost:3000/vets');
    await expect(page.getByText('No results found.')).toBeVisible();
    await expect(page.getByText('Error fetching all vets.')).toHaveCount(0);
  });

  test('failed request displays an error instead of a blank profile', async ({
    page,
  }) => {
    await page.route('**/api/v2/gateway/vets', async route => {
      await route.fulfill({ status: 503, json: { message: 'Unavailable' } });
    });
    await page.goto('http://localhost:3000/vets');
    await expect(page.getByText('Error fetching all vets.')).toBeVisible();
    await expect(page.locator('.card-vets')).toHaveCount(0);
  });
});
