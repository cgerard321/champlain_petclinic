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

test('Get User By Id works and connected customer link works and right info', async ({
  page,
}) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('button', { name: 'Users' }).click();
  await page.getByRole('link', { name: 'Users List' }).click();
  await page.getByRole('link', { name: 'e6c7398e-8ac4-4e10-9ee0-' }).click();
  await expect(
    page.getByRole('heading', { name: 'User Details for Owner2' })
  ).toBeVisible();
  await expect(page.getByText('User ID: e6c7398e-8ac4-4e10-')).toBeVisible();
  await expect(page.getByText('Email: betty@email.com')).toBeVisible();
  await expect(page.getByText('Verified: Yes')).toBeVisible();
  await expect(page.getByText('Roles: OWNER')).toBeVisible();
  await page.getByRole('button', { name: 'Connected Owner Info' }).click();
  await expect(
    page.getByRole('heading', { name: 'Customer Details for Betty' })
  ).toBeVisible();
  await expect(page.getByText('Pet ID: ecb109cd-57ea-4b85-')).toBeVisible();

  await page.close();
});

test('Admin Edit Customer', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('button', { name: 'Customers' }).click();
  await page.getByRole('link', { name: 'Customers List' }).click();
  await page.getByRole('link', { name: 'e6c7398e-8ac4-4e10-9ee0-' }).click();
  await page.getByRole('button', { name: 'Edit Customer' }).click();
  await page.locator('input[name="firstName"]').click();
  await page.locator('input[name="firstName"]').fill('John');
  await page.locator('input[name="lastName"]').click();
  await page.locator('input[name="lastName"]').fill('Doe');
  await page.locator('input[name="address"]').click();
  await page.locator('input[name="address"]').fill('Baker Street 123');
  await page.locator('input[name="city"]').click();
  await page.locator('input[name="city"]').fill('Montreal');
  await page.getByRole('combobox').selectOption('Quebec');
  await page.locator('input[name="telephone"]').click();
  await page.locator('input[name="telephone"]').fill('5144203239');
  await page.getByRole('button', { name: 'Update' }).click();
  await expect(page.locator('h2')).toContainText('Success!');
  await expect(page.getByRole('paragraph')).toContainText('Customer has been successfully updated.');
  await page.getByRole('button', { name: 'Close' }).click();
  await expect(page.locator('#root')).toContainText('First Name: John');
  await expect(page.locator('#root')).toContainText('Last Name: Doe');
  await expect(page.locator('#root')).toContainText('City: Montreal');
  await expect(page.locator('#root')).toContainText('Province: Quebec');
  await expect(page.locator('#root')).toContainText('Telephone: 5144203239');
  await page.close();
});
