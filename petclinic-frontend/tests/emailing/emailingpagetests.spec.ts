import { test, expect } from '@playwright/test';

test('Sending Email Test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Emails' }).click();
  await page.getByRole('button', { name: 'Send Raw Email' }).click();
  await page.locator('input[type="email"]').click();
  await page.locator('input[type="email"]').fill('xilef992@gmail.com');
  await page.locator('input[type="email"]').press('Tab');
  await page.locator('input[type="text"]').fill('TestEmail');
  await page.locator('input[type="text"]').press('Tab');
  await page.locator('textarea').fill('This is a test email');
  await page.getByRole('button', { name: 'Submit' }).click();
  await expect(page.getByText('Status Code:')).toBeVisible();
  await expect(page.locator('#root')).toContainText(
    'Message: Email sent successfully'
  );

  await page.close();
});
