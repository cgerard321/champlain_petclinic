import { test, expect } from '@playwright/test';

test('Update pet', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('button', { name: 'Customers' }).click();
  await page.getByRole('link', { name: 'Customers List' }).click();
  await page.getByRole('link', { name: 'f470653d-05c5-4c45-b7a0-' }).click();
  await page.getByRole('button', { name: 'Edit Pet' }).click();
  await page.locator('input[name="name"]').click();
  await page.locator('input[name="name"]').fill('Leawda');
  await page.getByRole('combobox').selectOption('3');
  await page.getByRole('checkbox').check();
  await page.getByRole('button', { name: 'Update Pet' }).click();
  await page.getByRole('button', { name: 'Close' }).click();
  await page.getByRole('button', { name: 'Edit Pet' }).click();
  await page.close();
});
