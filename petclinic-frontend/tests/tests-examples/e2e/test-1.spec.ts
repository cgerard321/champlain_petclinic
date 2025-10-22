import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.locator('.card-image').first().click();
  await page.getByRole('button', { name: 'Add Education' }).nth(1).click();
  await page.getByRole('button', { name: 'Add Education' }).nth(1).click();
  await page
    .locator('div')
    .filter({ hasText: /^School Name$/ })
    .getByRole('textbox')
    .click();
  await page
    .locator('div')
    .filter({ hasText: /^School Name$/ })
    .getByRole('textbox')
    .fill('Champlain College');
  await page
    .locator('div')
    .filter({ hasText: /^Degree$/ })
    .getByRole('textbox')
    .click();
  await page
    .locator('div')
    .filter({ hasText: /^Degree$/ })
    .getByRole('textbox')
    .fill('Cegep');
  await page
    .locator('div')
    .filter({ hasText: /^Field of Study$/ })
    .getByRole('textbox')
    .click();
  await page
    .locator('div')
    .filter({ hasText: /^Field of Study$/ })
    .getByRole('textbox')
    .fill('Computer Science');
  await page
    .locator('div')
    .filter({ hasText: /^Start Date$/ })
    .getByRole('textbox')
    .fill('2021-08-23');
  await page
    .locator('div')
    .filter({ hasText: /^End Date$/ })
    .getByRole('textbox')
    .fill('2025-05-16');
  await page
    .getByRole('dialog')
    .getByRole('button', { name: 'Add Education' })
    .click();
  await page.getByRole('button', { name: 'Delete Education' }).nth(2).click();
});
