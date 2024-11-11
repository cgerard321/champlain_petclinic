import { test, expect } from '@playwright/test';

test('View Shop Product Title', async ({ page }) => {
  await page.goto('http://localhost:3000/home');
  await page.getByRole('link', { name: 'Login' }).click();
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Shop' }).click();
  await page.getByRole('heading', { name: 'Aquarium Filter' }).first().click();
  await expect(
    page.getByRole('heading', { name: 'Aquarium Filter', exact: true })
  ).toContainText('Aquarium Filter');
});
