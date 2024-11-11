import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('george@email.com');
  await page.getByPlaceholder('Enter your email').press('Tab');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Bills' }).click();
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('row', { name: 'a7c71008-30d9-4166-8344-' }).getByRole('button').click();
  const download = await downloadPromise;
});