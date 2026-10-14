import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.getByRole('heading', { name: 'Edward Carter' }).click();
  await expect(
    page.getByRole('button', { name: 'Update Profile' })
  ).toBeVisible();
  await page.getByRole('button', { name: 'Update Profile' }).click();
  await expect(
    page
      .locator('div')
      .filter({ hasText: /^Friday$/ })
      .getByRole('checkbox')
  ).toBeVisible();
  await page
    .locator('div')
    .filter({ hasText: /^Friday$/ })
    .getByRole('checkbox')
    .uncheck();
  await expect(page.getByRole('button', { name: 'Update Vet' })).toBeVisible();
  await page.getByRole('button', { name: 'Update Vet' }).click();

  /*
!!-------NOTE------!!

Testing the text boxes on the front-end is note working as the browser is not find the boxes for some reason.

The test was conducted on the "Friday" checkbox. W

WebKit browser is not working for testing un-check it to validate test .
*/
});
