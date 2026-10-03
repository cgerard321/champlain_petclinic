import { test, expect } from '@playwright/test';

test('client can cancel a visit with a reason', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');

  await page
      .getByPlaceholder('Enter your email or username')
      .fill('george@email.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await expect(page).toHaveURL('http://localhost:3000/home');

  await page.goto('http://localhost:3000/customer/visits');

  const visitLink = page.getByRole('link', {
    name: 'VIST-2304-0101',
  });

  await expect(visitLink).toBeVisible();

  const visitId = await visitLink.innerText();

  const visitResponsePromise = page.waitForResponse(
      response =>
          response.request().method() === 'GET' &&
          response.url().includes(`/gateway/visits/${visitId}`)
  );

  await visitLink.click();

  const visit = await (await visitResponsePromise).json();

  await expect(page).toHaveURL(
      `http://localhost:3000/visits/${visit.visitId}`
  );

  const statusValue = page
      .locator('.visit-field')
      .filter({ hasText: 'Status:' })
      .locator('.visit-value');

  await expect(statusValue).toHaveText('CONFIRMED');

  await page.locator('.btn-cancel').click();

  await expect(page.getByText('Cancel Visit')).toBeVisible();

  await page
      .getByLabel('Appointment is no longer needed')
      .check();

  const cancelResponsePromise = page.waitForResponse(
      response =>
          response.request().method() === 'PATCH' &&
          response.url().includes(`/gateway/visits/${visitId}`)
  );

  await page
      .getByRole('button', { name: 'Confirm Cancellation' })
      .click();

  const cancelResponse = await cancelResponsePromise;

  expect(cancelResponse.ok()).toBeTruthy();

  const cancelledVisit = await cancelResponse.json();

  expect(cancelledVisit.cancellationReason).toBe(
      'APPOINTMENT_NO_LONGER_NEEDED'
  );

  await expect(statusValue).toHaveText('CANCELLED');
});