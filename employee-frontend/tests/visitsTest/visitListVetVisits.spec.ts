import { test, expect } from '@playwright/test';

test('vet sees all of their visits', async ({ page }) => {
  await page.goto('http://localhost:4200/login');

  await page.getByLabel("Nom d'utilisateur ou courriel").fill('vet1');
  await page.getByLabel('Mot de passe').fill('pwd');
  await page.getByRole('button', { name: 'Se connecter' }).click();

  await expect(page).toHaveURL('http://localhost:4200/home');

  // Get the logged-in vet's ID
  const vetResponsePromise = page.waitForResponse(
    (response) =>
      response.request().method() === 'GET' && response.url().includes('/api/gateway/users/jwt'),
  );

  await page.goto('http://localhost:4200/vist');

  const vetResponse = await vetResponsePromise;
  const user = await vetResponse.json();
  const practitionerId = user.userId;

  // Wait for the visits request for this specific vet
  const visitsVetResponsePromise = page.waitForResponse(
    (response) =>
      response.request().method() === 'GET' &&
      response.url().includes(`/api/gateway/visits/vets/${practitionerId}/visits`),
  );

  await page.reload();

  const visitsResponse = await visitsVetResponsePromise;

  expect(visitsResponse.url()).toContain(`/api/gateway/visits/vets/${practitionerId}/visits`);

  // Parse the SSE response
  const responseText = await visitsResponse.text();

  const backendVisits = responseText
    .split(/\r?\n\r?\n/)
    .filter((event) => event.includes('data:'))
    .map((event) => {
      const data = event.split(/\r?\n/).find((line) => line.startsWith('data:'));

      return JSON.parse(data!.replace(/^data:\s*/, ''));
    });

  // Get the visits displayed in the table
  const rowsVisits = page.locator('table tr');

  await expect(rowsVisits.nth(1)).toBeVisible({ timeout: 10000 });

  const rowCount = await rowsVisits.count();
  const displayedVisitIds: string[] = [];

  // Start at 1 because row 0 is the table header
  for (let i = 1; i < rowCount; i++) {
    const visitId = await rowsVisits.nth(i).locator('td').nth(0).innerText();
    displayedVisitIds.push(visitId.trim());
  }

  // Get Visit IDs from the backend
  const backendVisitIds = backendVisits.map((visit) => visit.visitId);

  // Make sure the number of visits is the same
  expect(displayedVisitIds.length).toBe(backendVisitIds.length);

  // Sort them in case the backend and table use different ordering
  displayedVisitIds.sort();
  backendVisitIds.sort();

  // Make sure the actual Visit IDs are the same
  expect(displayedVisitIds).toEqual(backendVisitIds);
});
