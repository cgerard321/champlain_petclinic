import { test, expect } from '@playwright/test';

// eslint-disable-next-line playwright/require-top-level-describe
test('addEmergencyVisit', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').fill('owner2');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForURL(url => !url.pathname.startsWith('/users/login'));

  await page.goto('http://localhost:3000/customer/visits');
  console.log('URL after goto:', page.url());
  await page.screenshot({ path: 'after-goto.png', fullPage: true });
  await expect(page.getByRole('button', { name: 'Create' })).toBeVisible();

  await page.locator('label[for="isEmergency"]').click();

  await page.locator('#petId').selectOption({ index: 1 });

  const emergencyType = page.locator('#emergencyType');
  await emergencyType.selectOption('surgical');
  await expect(emergencyType).toHaveValue('surgical');

  await page.locator('#description').fill('Deep wound on the leg');

  const vetOptions = page.locator('#practitionerId option');
  await expect(vetOptions).not.toHaveCount(1);
  await page.locator('#practitionerId').selectOption({ index: 1 });

  await page.locator('.emergency-vet-section .time-slot').first().click();

  await page.getByRole('button', { name: 'Add', exact: true }).click();
});
