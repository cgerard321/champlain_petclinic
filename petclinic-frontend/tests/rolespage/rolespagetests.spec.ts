import { test, expect } from '@playwright/test';

test('Admin updates a role', async ({ page }) => {
  
    await page.goto('http://localhost:3000/users/login');
    await page.getByPlaceholder('Enter your email').click();
    await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
    await page.getByPlaceholder('Enter your email').press('Tab');
    await page.getByPlaceholder('Enter your password').fill('pwd');
    await page.getByRole('button', { name: 'Login' }).click();
    await page.getByRole('button', { name: 'Users' }).click();
    await page.getByRole('link', { name: 'Roles List' }).click();
    await page.getByRole('button', { name: 'Create Role' }).click();
    await page.getByPlaceholder('Role Name').click();
    await page.getByPlaceholder('Role Name').fill('security');
    await page.getByRole('button', { name: 'Confirm' }).click();
    await expect(page.locator('tbody')).toContainText('SECURITY');

    await page.close();
});
