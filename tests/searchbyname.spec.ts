import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your email').press('Tab');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.getByPlaceholder('Search by first name, last').click();
  await page.getByPlaceholder('Search by first name, last').fill('James');
  await page.getByRole('button', { name: 'Search' }).click();
  await expect(page.locator('div').filter({ hasText: 'James CarterSpecialties:' }).nth(3)).toBeVisible();
  await page.close();
});