import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/home');
  await page.getByRole('link', { name: 'Signup' }).click();
  await page.locator('input[name="firstName"]').click();
  await page.locator('input[name="firstName"]').fill('ew');
  await page.locator('input[name="lastName"]').click();
  await page.locator('input[name="lastName"]').fill('ew');
  await page.locator('input[name="address"]').click();
  await page.locator('input[name="address"]').fill('jesse');
  await page.locator('input[name="city"]').click();
  await page.locator('input[name="city"]').fill('stjean');
  await page.locator('input[name="province"]').click();
  await page.locator('input[name="province"]').fill('qc');
  await page.locator('input[name="telephone"]').click();
  await page.locator('input[name="telephone"]').fill('1234567890');
  await page.locator('input[name="username"]').click();
  await page.locator('input[name="username"]').fill('wewew');
  await page.locator('input[name="email"]').click();
  await page.locator('input[name="email"]').fill('wrongemail');
  await page.locator('input[name="password"]').click();
  await page.locator('input[name="password"]').press('CapsLock');
  await page.locator('input[name="password"]').fill('P');
  await page.locator('input[name="password"]').press('CapsLock');
  await page.locator('input[name="password"]').fill('Password12:');
  await page.getByRole('button', { name: 'Send Verification Email' }).click();
  await expect(page.locator('form')).toContainText('Invalid email format.');
});
