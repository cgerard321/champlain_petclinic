import { test, expect } from '@playwright/test';

test('test visit details', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');

  await page
    .getByPlaceholder('Enter your email or username')
    .fill('george@email.com');

  await page.getByPlaceholder('Enter your password').fill('pwd');

  await page.getByRole('button', { name: 'Login' }).click();

  await expect(page).toHaveURL('http://localhost:3000/home');

  await page.goto('http://localhost:3000/customer/visits');

  const visitLink = page.getByRole('link', {
    name: 'VIST-2212-2401',
  });

  const visitId = await visitLink.innerText();

  const visitResponsePromise = page.waitForResponse(
    response =>
      response.request().method() === 'GET' &&
      response.url().includes(`/gateway/visits/${visitId}`)
  );

  await visitLink.click();

  const visitResponse = await visitResponsePromise;

  const visit = await visitResponse.json();

  await expect(page).toHaveURL(`http://localhost:3000/visits/${visit.visitId}`);

  const detailsVisitId = await page
    .locator('.visit-field')
    .filter({ hasText: 'Visit ID:' })
    .locator('.visit-value')
    .innerText();

  const detailsVisitDate = await page
    .locator('.visit-field')
    .filter({ hasText: 'Visit Date:' })
    .locator('.visit-value')
    .innerText();

  const detailsDescription = await page
    .locator('.visit-field')
    .filter({ hasText: 'Description:' })
    .locator('.visit-value')
    .innerText();

  const detailsPetName = await page
    .locator('.visit-field')
    .filter({ hasText: 'Pet Name:' })
    .locator('.visit-value')
    .innerText();

  const detailsVetFirstName = await page
    .locator('.visit-field')
    .filter({ hasText: 'Vet First Name:' })
    .locator('.visit-value')
    .innerText();

  const detailsVetLastName = await page
    .locator('.visit-field')
    .filter({ hasText: 'Vet Last Name:' })
    .locator('.visit-value')
    .innerText();

  const detailsVetEmail = await page
    .locator('.visit-field')
    .filter({ hasText: 'Vet Email:' })
    .locator('.visit-value')
    .innerText();

  const detailsStatus = await page
    .locator('.visit-field')
    .filter({ hasText: 'Status:' })
    .locator('.visit-value')
    .innerText();

  const detailsVisitEndDate = await page
    .locator('.visit-field')
    .filter({ hasText: 'Visit End Date:' })
    .locator('.visit-value')
    .innerText();

  expect(detailsVisitId).toBe(visit.visitId);
  expect(detailsVisitDate).toBe(visit.visitDate);
  expect(detailsDescription).toBe(visit.description);
  expect(detailsPetName).toBe(visit.petName);
  expect(detailsVetFirstName).toBe(visit.vetFirstName);
  expect(detailsVetLastName).toBe(visit.vetLastName);
  expect(detailsVetEmail).toBe(visit.vetEmail);
  expect(detailsStatus).toBe(visit.status);
  expect(detailsVisitEndDate).toBe(visit.visitEndDate);
});
