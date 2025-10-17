import { test, expect } from '@playwright/test';

test.describe('Login Functionality', () => {
  test('Login page loads correctly', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('input[type="email"], input[name="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"], input[name="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"], button:has-text("Login")')).toBeVisible();
  });

  test('Login form can be filled and submitted', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.locator('input[type="email"], input[name="email"]').fill('test@example.com');
    await page.locator('input[type="password"], input[name="password"]').fill('testpassword');
    
    await expect(page.locator('input[type="email"], input[name="email"]')).toHaveValue('test@example.com');
    await expect(page.locator('input[type="password"], input[name="password"]')).toHaveValue('testpassword');
    
    await expect(page.locator('button[type="submit"], button:has-text("Login")')).toBeEnabled();
  });

  test('Login with valid credentials redirects correctly', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.locator('input[type="email"], input[name="email"]').fill('admin@admin.com');
    await page.locator('input[type="password"], input[name="password"]').fill('pwd');
    await page.locator('button[type="submit"], button:has-text("Login")').click();
    
    await page.waitForLoadState('networkidle');
    await expect(page.locator('body')).toContainText('Welcome');
  });
});
