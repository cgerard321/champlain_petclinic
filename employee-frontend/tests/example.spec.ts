import { test, expect } from '@playwright/test';

test('Champlain College Employee Portal loads successfully', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveTitle(/Employee Frontend/);

  await expect(page.locator('h1')).toContainText('Champlain College');

  await expect(page.locator('.subtitle')).toContainText('Employee Management Portal');
});

test('Feature cards are visible and functional', async ({ page }) => {
  await page.goto('/');

  const featureCards = page.locator('.feature-card');
  await expect(featureCards).toHaveCount(6);

  await expect(page.locator('text=Customer Management')).toBeVisible();
  await expect(page.locator('text=Appointment Booking')).toBeVisible();
  await expect(page.locator('text=Veterinarian Portal')).toBeVisible();
  await expect(page.locator('text=Inventory Management')).toBeVisible();
  await expect(page.locator('text=Billing System')).toBeVisible();
  await expect(page.locator('text=Product Store')).toBeVisible();
});

test('Role badges are displayed correctly', async ({ page }) => {
  await page.goto('/');

  await expect(page.locator('text=Available for:')).toBeVisible();

  await expect(page.locator('.role-badge:has-text("Receptionist")')).toBeVisible();
  await expect(page.locator('.role-badge:has-text("Veterinarian")')).toBeVisible();
  await expect(page.locator('.role-badge:has-text("Inventory Manager")')).toBeVisible();
  await expect(page.locator('.role-badge:has-text("Administrator")')).toBeVisible();
});

test('Pets section displays correctly', async ({ page }) => {
  await page.goto('/');

  await expect(page.locator('.pets-heading')).toContainText('Every animal deserves love');

  await expect(page.locator('img[alt*="Larry"]')).toBeVisible();
});

test('Status information is displayed', async ({ page }) => {
  await page.goto('/');

  await expect(page.locator('.status-text')).toContainText('New Angular 20 Employee Portal');
  await expect(page.locator('.migration-note')).toContainText('Modern Angular 20 with Vite');

  await expect(page.locator('.progress-bar')).toBeVisible();
});

test('Bootstrap icons are loaded', async ({ page }) => {
  await page.goto('/');

  const icons = page.locator('.bi');
  await expect(icons.first()).toBeVisible();
});

test('Page has no console errors', async ({ page }) => {
  const errors: string[] = [];

  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
    }
  });

  await page.goto('/');

  await page.waitForTimeout(1000);

  expect(errors).toHaveLength(0);
});
