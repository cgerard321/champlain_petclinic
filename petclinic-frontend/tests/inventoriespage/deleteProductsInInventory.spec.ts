import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('link', { name: 'Inventories' })).toBeVisible();
  await page.getByRole('link', { name: 'Inventories' }).click();
  await page.getByText('Vaccines').click();
  await expect(
    page.getByRole('button', { name: 'Delete' }).first()
  ).toBeVisible();
  await page.getByRole('button', { name: 'Delete' }).first().click();
  await expect(page.getByRole('button', { name: 'Confirm' })).toBeVisible();
  await page.getByRole('button', { name: 'Confirm' }).click();
});
