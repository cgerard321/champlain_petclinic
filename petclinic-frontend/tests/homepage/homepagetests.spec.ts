import { test, expect } from '@playwright/test';

test('See Vet Details from Homepage', async ({ page }) => {
  //Authenticate as admin
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForURL('http://localhost:3000/home');

  await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();
  // End of authentication steps.

  await expect(page.locator('.vet-card')).toHaveCount(3);

  await page.locator('.vet-card').first().click();

  await expect(page.locator('.vet-details-container')).toContainText(
    'Vet Information'
  );

  await page.close();
});
