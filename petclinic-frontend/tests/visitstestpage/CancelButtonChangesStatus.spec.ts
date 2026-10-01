import { test, expect } from '@playwright/test';

test('test cancel button visibility', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');

  await page.getByPlaceholder('Enter your email or username').fill('george@email.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await expect(page).toHaveURL('http://localhost:3000/home');

  await page.goto('http://localhost:3000/customer/visits');


  const visitLink = page.locator('table tbody a').first();
  await expect(visitLink).toBeVisible();
  const visitId = await visitLink.innerText();


  const visitResponsePromise = page.waitForResponse(
    response =>
      response.request().method() === 'GET' &&
      response.url().includes(`/gateway/visits/${visitId}`)
  );

  await visitLink.click();

  const visit = await (await visitResponsePromise).json();

  await expect(page).toHaveURL(`http://localhost:3000/visits/${visit.visitId}`);

 
  await expect(page.getByText('Visit Details')).toBeVisible();
  await expect(
    page.locator('.visit-field').filter({ hasText: 'Status:' }).locator('.visit-value')
  ).toHaveText(visit.status);

  const statusValue = page
    .locator('.visit-field')
    .filter({ hasText: 'Status:' })
    .locator('.visit-value');
  await expect(statusValue).toHaveText(visit.status);

  const cancelButton = page.locator('.btn-cancel');

  if (visit.status === 'CONFIRMED' || visit.status === 'UPCOMING') {
    const cancelResponsePromise = page.waitForResponse(
      response =>
        response.request().method() === 'PATCH' &&
        response.url().includes(`/gateway/visits/${visitId}/status/CANCELLED`)
    );
    await cancelButton.click();
    await cancelResponsePromise;
    await expect(statusValue).toHaveText('CANCELLED');
    await expect(cancelButton).toHaveCount(0);
  } else {
    await expect(cancelButton).toHaveCount(0);
  }



})