import { test, expect } from '@playwright/test';

// Extracted credentials to avoid hardcoded values in tests
const TEST_ADMIN_EMAIL = process.env['TEST_ADMIN_EMAIL'] || 'admin@admin.com';
const TEST_ADMIN_PASSWORD = process.env['TEST_ADMIN_PASSWORD'] || 'pwd';

test.describe('Homepage Tests', () => {
  test('should authenticate and display welcome title, introduction, and clinic logo', async ({ page }) => {
    // 1. Navigate to login route
    await page.goto('/users/login');

    // 2. Perform authentication
    const emailInput = page.locator('input[formcontrolname="email"], input[id="email"], input').nth(0);
    const passwordInput = page.locator('input[formcontrolname="password"], input[type="password"], input').nth(1);
    const loginButton = page.locator('button[type="submit"], button:has-text("Login"), button:has-text("Sign in")').first();

    await emailInput.fill(TEST_ADMIN_EMAIL);
    await passwordInput.fill(TEST_ADMIN_PASSWORD);
    await loginButton.click();

    // 3. Wait for navigation to homepage
    await page.waitForURL('**/home');

    // 4. Assert welcome badge is visible
    await expect(page.getByText('EMPLOYEE PORTAL')).toBeVisible();

    // 5. Assert welcome title heading
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();

    // 6. Assert short introduction text
    await expect(
      page.getByText('At Champlain Pet Clinic, we offer a wide range of services to ensure the health')
    ).toBeVisible();

    // 7. Assert clinic logo brand emblem and exact brand title
    await expect(page.getByText('PetClinic', { exact: true })).toBeVisible();
    await expect(page.locator('.brand-emblem')).toBeVisible();

    // Cleanup
    await page.close();
  });

  test('should render responsively on mobile viewports', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });

    await page.goto('/users/login');

    const emailInput = page.locator('input[formcontrolname="email"], input[id="email"], input').nth(0);
    const passwordInput = page.locator('input[formcontrolname="password"], input[type="password"], input').nth(1);
    const loginButton = page.locator('button[type="submit"], button:has-text("Login"), button:has-text("Sign in")').first();

    await emailInput.fill(TEST_ADMIN_EMAIL);
    await passwordInput.fill(TEST_ADMIN_PASSWORD);
    await loginButton.click();

    await page.waitForURL('**/home');

    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
    await expect(page.locator('.brand-emblem')).toBeVisible();

    await page.close();
  });
});
