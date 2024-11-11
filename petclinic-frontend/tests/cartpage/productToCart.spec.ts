import { test, expect } from '@playwright/test';

test('Admin Add new customer', async ({ page }) => {
  //Authenticate as admin
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('betty@email.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForURL('http://localhost:3000/home');

  await page.close();
});
