import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');

  await page.waitForSelector('[placeholder="Enter your email"]');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');

  await page.waitForSelector('[placeholder="Enter your password"]');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');

  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForSelector('role=link[name="Shop"]');
  await page.getByRole('link', { name: 'Shop' }).click();

  await page.waitForSelector('[placeholder="Search for a product..."]');
  await page.getByPlaceholder('Search for a product...').click();
  await page.getByPlaceholder('Search for a product...').fill('horse');

  await page.waitForSelector('role=listitem >> text=Horse Saddle');
  await expect(
    page.getByRole('listitem').getByText('Horse Saddle')
  ).toBeVisible();
  await page.close();
});
