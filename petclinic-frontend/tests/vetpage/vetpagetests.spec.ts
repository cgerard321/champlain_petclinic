import { test, expect } from '@playwright/test';

test('Admin View Vet Details from VetPage', async ({ page }) => {
  //Authenticate as admin
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForURL('http://localhost:3000/home');

  await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();
  // End of authentication steps.

  // Go to vets page
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.waitForURL('http://localhost:3000/vets');

  await expect(page.locator('.card-container')).toBeVisible();

  await page.locator('.card-vets').first().click();

  await expect(page.locator('.vet-details-container')).toContainText(
    'Vet Information'
  );

  await page.close();
});

test.fixme('Admin Delete Vet', async ({ page }) => {
  //Authenticate as admin
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await page.waitForURL('http://localhost:3000/home');

  await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();
  // End of authentication steps.

  // Go to vets page
  //await page.goto('http://localhost:3000/vets');
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.waitForURL('http://localhost:3000/vets');

  //find vetCard to be deleted

  //const vetCards = page.locator('.card-vets');
  await expect(page.locator('.card-vets')).toHaveCount(7);
  await expect(
    page.locator('.card-vets').locator('.btn').first()
  ).toBeVisible();
  //await page.locator('.card-vets').getByRole('button').first().click({ force: true });
  await expect(
    page.locator(
      'div.card-container > div:nth-child(1) > div.card-content > div > button'
    )
  ).toBeVisible();
  await page
    .locator(
      'div.card-container > div:nth-child(1) > div.card-content > div > button'
    )
    .click({ force: true });

  page.on('dialog', async dialog => {
    expect(dialog.type()).toBe('confirm');
    expect(dialog.message()).toBe('Are you sure you want to delete this vet?');
    await dialog.accept();
  });
  // Confirm delete
  await expect(page.locator('.card-vets')).toHaveCount(6);
});
