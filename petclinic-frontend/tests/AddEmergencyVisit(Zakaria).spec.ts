import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  //start
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('betty@email.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Emergency' }).click();
  await page.getByRole('button', { name: 'Create Emergency visit' }).click();
  await page.locator('input[name="petName"]').click();
  await page.locator('input[name="petName"]').fill('ham');
  await page.locator('textarea[name="description"]').click();
  await page.locator('textarea[name="description"]').fill('c');
  await page.getByText('c', { exact: true }).fill('cast');
  await page.locator('input[name="emergencyType"]').click();
  await page.locator('input[name="emergencyType"]').fill('death');
  await page.getByRole('combobox').selectOption('MEDIUM');
  await page.locator('input[name="petId"]').click();
  await page.locator('input[name="petId"]').click();
  await page
    .locator('input[name="petId"]')
    .fill('ecb109cd-57ea-4b85-b51e-99751fd1c349');
  await page.locator('input[name="practitionerId"]').click();
  await page.locator('input[name="practitionerId"]').click();
  await page
    .locator('input[name="practitionerId"]')
    .fill('69f85d2e-625b-11ee-8c99-0242ac120002');
  await page.getByRole('button', { name: 'Submit Emergency' }).click();
});
