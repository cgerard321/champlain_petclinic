import { test, expect } from '@playwright/test';

test('Unlist Product', async ({ page }) => {
//Authenticate as admin
await page.goto('http://localhost:3000/users/login');
await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
await page.getByPlaceholder('Enter your password').fill('pwd');
await page.getByRole('button', { name: 'Login' }).click();

await page.waitForURL('http://localhost:3000/home');

await page.getByRole('link', { name: 'Shop' }).click();

await page.getByRole('heading', { name: 'Fish Tank Heater' }).first().click();
await page.getByRole('button', { name: 'Unlist Item' }).click();
await page.getByRole('button', { name: 'Yes' }).click();

await expect(page.getByRole('button', { name: 'List Item' })).toBeVisible();

await page.close();

});