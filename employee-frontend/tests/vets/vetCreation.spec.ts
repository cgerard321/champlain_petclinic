import { expect, test } from '@playwright/test';

test('admin creates a veterinarian and the directory refreshes', async ({ page }) => {
  const profiles: Record<string, unknown>[] = [];
  let creates = 0;
  await page.route('**/api/**', (route) => route.fulfill({ json: [] }));
  await page.route('**/api/gateway/users/jwt', (route) =>
    route.fulfill({ json: { userId: 'admin', username: 'admin', roles: ['ADMIN'] } }),
  );
  await page.route('**/api/v2/gateway/vets', (route) =>
    route.fulfill({
      contentType: 'text/event-stream',
      body: profiles.map((v) => `data:${JSON.stringify(v)}\n\n`).join(''),
    }),
  );
  await page.route('**/api/v2/gateway/vets/users/vets', async (route) => {
    creates++;
    const body = route.request().postDataJSON();
    expect(body.username).toBe('alex');
    const vet = { ...body.vet, vetId: '11111111-1111-4111-8111-111111111111' };
    profiles.push(vet);
    await route.fulfill({ status: 201, json: vet });
  });
  await page.goto('/vets');
  await page.locator('a[href="/vets/list?add=1"]').click();
  const save = page.locator('app-vet-create-form button[type="submit"]');
  await expect(save).toBeDisabled();
  for (const [field, value] of Object.entries({
    firstName: 'Alex',
    lastName: 'Morin',
    email: 'bad',
    username: 'alex',
    password: 'SafePass1!',
    phoneNumber: '514-555-0123',
    resume: 'Preventive animal care.',
  })) {
    await page.locator(`#create-${field}`).fill(value);
  }
  await page.locator('#create-specialties').selectOption({ label: 'Chirurgie' });
  await expect(save).toBeDisabled();
  await page.locator('#create-email').fill('alex@example.com');
  await expect(save).toBeEnabled();
  await page.screenshot({ path: 'test-results/vet-create-form.png', fullPage: true });
  await page.evaluate(() => document.documentElement.classList.add('dark-mode'));
  await page.setViewportSize({ width: 390, height: 844 });
  const form = page.locator('app-vet-create-form form');
  await expect(form).not.toHaveCSS('background-color', 'rgb(255, 255, 255)');
  const dimensions = await form.evaluate((element) => ({
    width: element.getBoundingClientRect().width,
    scroll: element.scrollWidth,
  }));
  expect(dimensions.scroll).toBeLessThanOrEqual(Math.ceil(dimensions.width));
  await page.screenshot({ path: 'test-results/vet-create-form-mobile-dark.png', fullPage: true });
  await save.click();
  await expect(page.locator('app-vet-create-form')).toHaveCount(0);
  await expect(page.locator('app-vet-profile-card')).toHaveCount(1);
  await expect(page.locator('app-vet-profile-card')).toContainText('Alex');
  expect(creates).toBe(1);
});

test('admin cancels without sending a creation request', async ({ page }) => {
  let creates = 0;
  await page.route('**/api/**', (route) => route.fulfill({ json: [] }));
  await page.route('**/api/gateway/users/jwt', (route) =>
    route.fulfill({ json: { userId: 'admin', username: 'admin', roles: ['ADMIN'] } }),
  );
  await page.route('**/api/v2/gateway/vets', (route) =>
    route.fulfill({ contentType: 'text/event-stream', body: '' }),
  );
  await page.route('**/api/v2/gateway/vets/users/vets', (route) => {
    creates++;
    return route.fulfill({ json: {} });
  });
  await page.goto('/vets/list?add=1');
  await page.locator('#create-firstName').fill('Alex');
  await page.locator('app-vet-create-form button[type="button"]').click();
  await expect(page.locator('app-vet-create-form')).toHaveCount(0);
  expect(creates).toBe(0);
});

for (const role of ['VET', 'RECEPTIONIST']) {
  test(`${role} cannot open the creation form`, async ({ page }) => {
    await page.route('**/api/**', (route) => route.fulfill({ json: [] }));
    await page.route('**/api/gateway/users/jwt', (route) =>
      route.fulfill({ json: { userId: 'employee', username: 'employee', roles: [role] } }),
    );
    await page.route('**/api/v2/gateway/vets', (route) =>
      route.fulfill({ contentType: 'text/event-stream', body: '' }),
    );
    await page.goto('/vets/list?add=1');
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('app-vet-create-form')).toHaveCount(0);
    await expect(page.locator('.vet-list-page > button')).toHaveCount(0);
  });
}
