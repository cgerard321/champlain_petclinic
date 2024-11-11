import { test, expect } from '@playwright/test';

test('Admin Add new customer', async ({ page }) => {
  //Authenticate as admin
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForURL('http://localhost:3000/home');

  await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();
  // End of authentication steps.

  // Go to Add Customer page
  await page.locator('#owners-dropdown').click();
  await page.getByRole('link', { name: 'Add Customer' }).click();

  await page.waitForURL('http://localhost:3000/customer/add');

  await expect(page.locator('.add-customer-form')).toBeVisible();
  await expect(page.locator('.add-customer-form')).toContainText(
    'Add Customer'
  );

  // Fill in the form
  await page.locator('input[name="firstName"]').fill('Christine');
  await page.locator('input[name="lastName"]').fill('Gerard');
  await page.locator('input[name="address"]').fill('123 Main St');
  await page.locator('input[name="city"]').fill('Montreal');
  await page.locator('input[name="province"]').fill('Quebec');
  await page.locator('input[name="telephone"]').fill('514-123-4567');

  await page.locator('button[type="submit"]').click();

  //at the moment, this functionality does not work well.
  //we stay stuck on the Add Customer page.

  //This code should be changed once page is fixed.
  // Go to Customers List page
  await page.locator('#owners-dropdown').click();
  await page.getByRole('link', { name: 'Customers List' }).click();

  //check that we have been routed to the correct page
  await page.waitForURL('http://localhost:3000/customers');
  await expect(page.locator('.owners-container')).toContainText('Owners');

  //check that customer has been added to customer list
  await expect(page.locator('tbody')).toContainText('Christine');
  await expect(page.locator('tbody')).toContainText('Gerard');

  await page.close();
});