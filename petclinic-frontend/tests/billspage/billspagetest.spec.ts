import { test, expect } from '@playwright/test';

test('Admin can filter bills by status', async ({ page }) => {
    //Authenticate as admin
    await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForURL('http://localhost:3000/home');

  await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();

  await page.getByRole('link', { name: 'Bills' }).click();
  await page.waitForURL('http://localhost:3000/bills/admin');



    await expect(page.getByRole('button', { name: 'Filter' })).toBeVisible();
    await page.getByRole('button', { name: 'Filter' }).click();
    await expect(page.getByRole('button', { name: 'Clear' })).toBeVisible();
    await page.getByRole('button', { name: 'Clear' }).click();
    await page.getByLabel('Year:').fill('');
    await expect(page.getByLabel('Status:')).toBeVisible();
    await page.getByLabel('Status:').selectOption('paid');
    await expect(page.locator('tbody')).toContainText('PAID');
    await expect(page.locator('tbody')).toContainText('PAID');
});