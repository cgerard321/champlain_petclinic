import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Inventories' }).click();
  await page.getByRole('textbox').nth(1).click();
  await page.getByRole('textbox').nth(1).fill('tools');
  await page.getByRole('button', { name: 'Search' }).click();
  await expect(page.getByText('Tools for performing surgical')).toBeVisible();
});
