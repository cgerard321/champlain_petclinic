import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Visits' }).click();

  //this times out. I assume because its not accurate enough. (dont have time to fix right now)
  await page
    .getByRole('row', { name: 'visitId1 11/30/2022, 8:00:00' })
    .getByRole('button')
    .nth(1)
    .click();
  await page.locator('input[name="visitStartDate"]').click();
  await page.locator('input[name="visitStartDate"]').fill('2022-11-17T13:00');
  await page.locator('input[name="description"]').click();
  await page
    .locator('input[name="description"]')
    .fill('This is new text222123');
  await page.locator('input[name="description"]').click();
  await page
    .locator('input[name="description"]')
    .fill('This is new text222123123123123');
  await page.getByRole('button', { name: 'Update' }).click();
});
