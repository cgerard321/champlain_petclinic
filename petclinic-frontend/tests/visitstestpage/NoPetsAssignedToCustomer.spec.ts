import { test, expect } from '@playwright/test';
// eslint-disable-next-line playwright/require-top-level-describe, playwright/expect-expect
export interface PetResponseModel {
  customerId: string;
  petId: string;
  name: string;
  birthDate: Date;
  petTypeId: string;
  isActive: string;
  weight: string;
  // photo?: FileResponseModel;
}

// eslint-disable-next-line playwright/require-top-level-describe
test('pet display', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('owner3');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL('http://localhost:3000/home');
  await page.goto('http://localhost:3000/customer/visits');

  const petsResponsePromise = page.waitForResponse(
    response =>
      /\/pets\/customers\/[^/]+\/pets(?:\?|$)/.test(response.url()) &&
      response.request().method() === 'GET'
  );
  await page.getByRole('button', { name: 'Create' }).click();
  const petsResponse = await petsResponsePromise;

  const requestUrl = new URL(petsResponse.url());
  const customerId = requestUrl.pathname.match(
    /\/pets\/customers\/([^/]+)\/pets$/
  )?.[1];

  expect(customerId).toBeTruthy();

  const responseText = await petsResponse.text();

  const pets = responseText
    .split('\n')
    .filter(line => line.startsWith('data:'))
    .map(line => JSON.parse(line.slice(5).trim()) as PetResponseModel);

  await expect(
    pets.length === 0
      ? page.getByText('No pet is assigned to you  ')
      : page.locator('select#petId')
  ).toBeVisible();
});
