import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Visits' }).click();
  await page.getByRole('button', { name: 'Make a Visit' }).click();
  await page.locator('input[name="petId"]').click();
  await page
    .locator('input[name="petId"]')
    .fill('0e4d8481-b611-4e52-baed-af16caa8bf8a');
  await page.locator('input[name="visitStartDate"]').click();
  await page.locator('input[name="visitStartDate"]').press('ArrowRight');
  await page.locator('input[name="visitStartDate"]').press('ArrowRight');
  await page.locator('input[name="visitStartDate"]').fill('2024-11-12T17:55');
  await page.locator('input[name="description"]').click();
  await page.locator('input[name="description"]').fill('doggo');
  await page.locator('input[name="practitionerId"]').click();
  await page
    .locator('input[name="practitionerId"]')
    .fill('69f85d2e-625b-11ee-8c99-0242ac120002');
  await page.getByRole('button', { name: 'Add' }).click();
});
