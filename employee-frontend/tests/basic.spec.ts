import { test, expect } from '@playwright/test';

test('Employee Frontend loads successfully', async ({ page }) => {
  await page.goto('/');
  
  await expect(page).toHaveTitle(/Employee Frontend/);
  
  await expect(page.locator('h1')).toContainText('Employee Frontend');
  await expect(page.locator('p')).toContainText('Coming Soon');
});

test('Layout components are visible', async ({ page }) => {
  await page.goto('/');
  
  await expect(page.locator('nav')).toBeVisible();
  await expect(page.locator('footer')).toBeVisible();
  await expect(page.locator('.alert-info')).toBeVisible();
});
