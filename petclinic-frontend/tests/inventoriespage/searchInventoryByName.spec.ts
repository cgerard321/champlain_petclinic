import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
    await page.goto('http://localhost:3000/users/login');
    await page.getByPlaceholder('Enter your email').click();
    await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
    await page.getByPlaceholder('Enter your password').click();
    await page.getByPlaceholder('Enter your password').fill('pwd');
    await page.getByRole('button', { name: 'Login' }).click();
    await page.getByRole('link', { name: 'Inventories' }).click();
    await page.getByRole('textbox').first().click();
    await page.getByRole('textbox').first().press('CapsLock');
    await page.getByRole('textbox').first().fill('M');
    await page.getByRole('textbox').first().press('CapsLock');
    await page.getByRole('cell', { name: 'M', exact: true }).getByRole('textbox').fill('Medical');
    await page.getByRole('button', { name: 'Search' }).click();
    await expect(page.getByText('Medical equipment', { exact: true })).toBeVisible();
});