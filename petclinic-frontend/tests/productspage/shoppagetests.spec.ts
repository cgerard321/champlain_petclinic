import { test, expect } from '@playwright/test';


test('Admin View Vet Details from VetPage', async ({ page }) => {
await page.goto('http://localhost:3000/users/login');
await page.getByPlaceholder('Enter your email').click();
await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
await page.getByPlaceholder('Enter your password').click();
await page.getByPlaceholder('Enter your password').fill('pwd');
await page.getByRole('button', { name: 'Login' }).click();
await page.getByRole('link', { name: 'Shop' }).click();
await page.getByRole('heading', { name: 'Fish Tank Heater' }).nth(1).click();
await page.getByRole('button', { name: 'Edit' }).click();
await page.getByLabel('Product Name:').click();
await page.getByLabel('Product Name:').fill('Fish Tank');
await page.getByLabel('Product Sale Price:').click();
await page.getByLabel('Product Sale Price:').fill('19.99');
await page.getByRole('button', { name: 'Update Product' }).click();
await (expect(page.getByRole('heading', {name: 'Fish Tank'})).toBeVisible());
});