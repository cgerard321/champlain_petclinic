import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Visits' }).click();
  await page
    .getByRole('row', { name: 'emergencyId2' })
    .getByRole('button')
    .nth(2)
    .click();
  //await page.getByRole('button', { name: 'Return to Emergencies' }).click();
});
