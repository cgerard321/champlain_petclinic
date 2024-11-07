import { test as setup, expect } from '@playwright/test';
//import path from 'path';

const adminFile = 'playwright/.auth/admin.json';
// const adminFile = path.join(
//   path.dirname(new URL(import.meta.url).pathname),
//   '../playwright/.auth/admin.json'
// );

setup('authenticate as admin', async ({ page }) => {
  // Perform authentication steps. Replace these actions with your own.
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your username').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  // Wait until the page receives the cookies.
  //
  // Sometimes login flow sets cookies in the process of several redirects.
  // Wait for the final URL to ensure that the cookies are actually set.
  await page.waitForURL('http://localhost:3000/home');
  // Alternatively, you can wait until the page reaches a state where all cookies are set.
  await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();

  // End of authentication steps.

  await page.context().storageState({ path: adminFile });
});

const owner1File = 'playwright/.auth/user.json';

setup('authenticate as user', async ({ page }) => {
  // Perform authentication steps. Replace these actions with your own.
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your username').fill('george@email.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  // Wait until the page receives the cookies.
  //
  // Sometimes login flow sets cookies in the process of several redirects.
  // Wait for the final URL to ensure that the cookies are actually set.
  await page.waitForURL('http://localhost:3000/home');
  // Alternatively, you can wait until the page reaches a state where all cookies are set.
  await expect(page.getByRole('button', { name: 'Owner1' })).toBeVisible();

  // End of authentication steps.

  await page.context().storageState({ path: owner1File });
});
