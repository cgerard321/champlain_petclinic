import { test, expect } from '@playwright/test';

test('admin sees all visits', async ({ page }) => {
  await page.goto('http://localhost:4200/login');

  await page.getByLabel("Nom d'utilisateur ou courriel").fill('admin@admin.com');
  await page.getByLabel('Mot de passe').fill('pwd');
  await page.getByRole('button', { name: 'Se connecter' }).click();

  await expect(page).toHaveURL('http://localhost:4200/home');

  let backendVisitCount = 0;

  page.on('response', async (response) => {
    if (response.request().method() === 'GET' && response.url().includes('/api/gateway/visits')) {
      const body = await response.body();

      const responseText = body.toString();

      backendVisitCount = responseText
        .split(/\r?\n\r?\n/)
        .filter((event: string) => event.includes('data:')).length;
    }
  });

  await page.goto('http://localhost:4200/vist');

  const rowsVisits = page.locator('table tr');

  // Wait for visits to start appearing
  await expect(rowsVisits.nth(1)).toBeVisible({ timeout: 10000 });

  // Wait until the table has finished receiving visits
  await page.waitForTimeout(3000);

  const rowVisitsCount = (await rowsVisits.count()) - 1;

  // console.log('Backend visits:', backendVisitCount);
  // console.log('Frontend visits:', rowVisitsCount);

  expect(backendVisitCount).toBe(rowVisitsCount);
});
