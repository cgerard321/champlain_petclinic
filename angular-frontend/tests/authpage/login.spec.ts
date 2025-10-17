import { test, expect } from '@playwright/test';

test.describe('Login Page Tests', () => {
  test('AngularJS app loads successfully', async ({ page }) => {
    await page.goto('/');
    
    await expect(page.locator('html')).toHaveAttribute('ng-app', 'petClinicApp');
    await expect(page).toHaveTitle(/PetClinic/);
  });

  test('Login page loads and displays all form elements', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return typeof (window as any).angular !== 'undefined';
    }, { timeout: 10000 });
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await expect(page.locator('h2')).toContainText('Login');
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('#email')).toBeVisible();
    await expect(page.locator('#pwd')).toBeVisible();
    await expect(page.locator('#button')).toBeVisible();
    await expect(page.locator('label:has-text("Email")')).toBeVisible();
    await expect(page.locator('label:has-text("Password")')).toBeVisible();
    await expect(page.locator('#email')).toHaveAttribute('name', 'email');
    await expect(page.locator('#email')).toHaveAttribute('required', '');
    await expect(page.locator('#pwd')).toHaveAttribute('type', 'password');
    await expect(page.locator('#pwd')).toHaveAttribute('name', 'password');
    await expect(page.locator('#pwd')).toHaveAttribute('required', '');
    await expect(page.locator('#button')).toHaveAttribute('type', 'submit');
  });

  test('Form fields are marked as required', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await expect(page.locator('#email')).toHaveAttribute('required', '');
    await expect(page.locator('#pwd')).toHaveAttribute('required', '');
    await expect(page.locator('.help-block:has-text("Email is required")')).toBeAttached();
    await expect(page.locator('.help-block:has-text("Password is required")')).toBeAttached();
  });

  test('Login form can be filled with credentials', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await page.locator('#email').fill('test@example.com');
    await page.locator('#pwd').fill('testpassword');
    
    await expect(page.locator('#email')).toHaveValue('test@example.com');
    await expect(page.locator('#pwd')).toHaveValue('testpassword');
    await expect(page.locator('#button')).toBeEnabled();
  });

  test('Login with invalid credentials shows error message', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await page.locator('#email').fill('invalid@example.com');
    await page.locator('#pwd').fill('wrongpassword');
    await page.locator('#button').click();
    
    await expect(page.locator('.alert-danger')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('.alert-danger')).toContainText('Error:');
  });

  test('Login with valid credentials redirects to welcome page', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await page.locator('#email').fill('admin@admin.com');
    await page.locator('#pwd').fill('pwd');
    await page.locator('#button').click();
    
    await page.waitForURL('**/#!/welcome', { timeout: 10000 });
    await page.waitForLoadState('networkidle');
    
    await expect(page).toHaveURL(/.*#!\/welcome/);
    await expect(page.locator('body')).toContainText('Welcome');
    
    const userEmail = await page.evaluate(() => localStorage.getItem('email'));
    const userRoles = await page.evaluate(() => localStorage.getItem('roles'));
    expect(userEmail).toBe('admin@admin.com');
    expect(userRoles).toContain('ADMIN');
  });

  test('Password visibility toggle works', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await page.locator('#pwd').fill('testpassword');
    
    await expect(page.locator('#pwd')).toHaveAttribute('type', 'password');
    
    await page.locator('#eye').click();
    await expect(page.locator('#pwd')).toHaveAttribute('type', 'text');
    
    await page.locator('#eye').click();
    await expect(page.locator('#pwd')).toHaveAttribute('type', 'password');
  });

  test('Error message can be dismissed', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await page.locator('#email').fill('invalid@example.com');
    await page.locator('#pwd').fill('wrongpassword');
    await page.locator('#button').click();
    
    await expect(page.locator('.alert-danger')).toBeVisible({ timeout: 10000 });
    
    await page.locator('.close').click();
    await expect(page.locator('.alert-danger')).not.toBeVisible();
  });

  test('Successful login persists user session', async ({ page }) => {
    await page.goto('/#!/login');
    await page.waitForLoadState('networkidle');
    
    await page.waitForFunction(() => {
      return document.querySelector('form') !== null;
    }, { timeout: 10000 });
    
    await page.locator('#email').fill('admin@admin.com');
    await page.locator('#pwd').fill('pwd');
    await page.locator('#button').click();
    
    await page.waitForURL('**/#!/welcome', { timeout: 10000 });
    
    const userData = await page.evaluate(() => ({
      username: localStorage.getItem('username'),
      email: localStorage.getItem('email'),
      uuid: localStorage.getItem('UUID'),
      roles: localStorage.getItem('roles')
    }));
    
    expect(userData.email).toBe('admin@admin.com');
    expect(userData.username).toBeTruthy();
    expect(userData.uuid).toBeTruthy();
    expect(userData.roles).toContain('ADMIN');
    
    await page.goto('/#!/');
    const persistedEmail = await page.evaluate(() => localStorage.getItem('email'));
    expect(persistedEmail).toBe('admin@admin.com');
  });
});