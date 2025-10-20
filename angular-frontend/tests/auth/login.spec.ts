import { test, expect } from '@playwright/test';

test('User can login successfully', async ({ page }) => {
  await page.goto('/login');
  
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
  
  await page.locator('#email').fill('admin@admin.com');
  await page.locator('#pwd').fill('pwd');
  
  await page.getByRole('button', { name: 'Login' }).click();
  
  await page.waitForURL('**/welcome');
  
  await expect(page.getByRole('link', { name: 'Admin-Panel' })).toBeVisible();
});

test('User cannot login with invalid credentials', async ({ page }) => {
  await page.goto('/login');
  
  await page.locator('#email').fill('invalid@email.com');
  await page.locator('#pwd').fill('wrongpassword');
  
  await page.getByRole('button', { name: 'Login' }).click();
  
  await expect(page.locator('.alert.alert-danger')).toBeVisible();
  
  await expect(page).toHaveURL('/login');
});

test('User can logout successfully', async ({ page }) => {
  await page.goto('/login');
  await page.locator('#email').fill('admin@admin.com');
  await page.locator('#pwd').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForURL('**/welcome');
  
  await page.waitForSelector('a:has-text("Logout")', { timeout: 10000 });
  await page.locator('a:has-text("Logout")').click();
  
  await page.waitForURL('/login');
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
});