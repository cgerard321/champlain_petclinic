import { expect, test } from '@playwright/test';

const VET_USERNAME = process.env['TEST_VET_USERNAME'] || 'vet3';
const VET_PASSWORD = process.env['TEST_VET_PASSWORD'] || 'pwd';
const OWNER_EMAIL = process.env['TEST_OWNER_EMAIL'] || 'george@email.com';
const OWNER_PASSWORD = process.env['TEST_OWNER_PASSWORD'] || 'pwd';
const API_URL = process.env['API_URL'] || 'http://localhost:8080';
const CUSTOMER_FRONTEND_URL = process.env['CUSTOMER_FRONTEND_URL'] || 'http://localhost:3000';

test.describe('Schedule a follow-up visit', () => {
  test('a veterinarian schedules a follow-up that appears for the owner', async ({ page }) => {
    test.setTimeout(60_000);
    await page.goto('/login');
    await page.getByLabel("Nom d'utilisateur ou courriel").fill(VET_USERNAME);
    await page.getByLabel('Mot de passe').fill(VET_PASSWORD);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page).toHaveURL(/\/home$/);

    const userResponse = await page.request.get(`${API_URL}/api/gateway/users/jwt`);
    expect(userResponse.ok()).toBe(true);
    const user = (await userResponse.json()) as { userId: string };

    const start = new Date();
    start.setDate(start.getDate() + 1);
    const end = new Date(start);
    end.setMonth(end.getMonth() + 6);
    const startDate = start.toISOString().slice(0, 10);
    const endDate = end.toISOString().slice(0, 10);
    const datesResponse = await page.request.get(
      `${API_URL}/api/gateway/visits/availability/vets/${user.userId}/dates`,
      { params: { startDate, endDate } },
    );
    expect(datesResponse.ok()).toBe(true);
    const availableDates = (await datesResponse.json()) as string[];
    expect(availableDates.length).toBeGreaterThan(0);

    await page.goto('/vist');
    const completedVisit = page
      .locator('tr')
      .filter({ hasText: 'Leo' })
      .filter({ hasText: 'COMPLETED' })
      .first();
    await expect(completedVisit).toBeVisible();
    await completedVisit
      .getByRole('button', { name: /Planifier un suivi|Schedule follow-up/ })
      .click();

    const date = new Date(`${availableDates[0]}T12:00:00`);
    await page.getByRole('button', { name: /Open calendar|Ouvrir le calendrier/ }).click();
    await page.locator('.mat-calendar-period-button').click();
    await page.getByRole('button', { name: date.getFullYear().toString(), exact: true }).click();
    await page.locator('button.mat-calendar-body-cell').nth(date.getMonth()).click();
    const slotsResponsePromise = page.waitForResponse(
      (response) =>
        response.request().method() === 'GET' &&
        response.url().includes(`/availability/vets/${user.userId}/slots`),
    );
    await page
      .locator('button.mat-calendar-body-cell')
      .filter({ hasText: new RegExp(`^\\s*${date.getDate()}\\s*$`) })
      .click();
    const slotsResponse = await slotsResponsePromise;
    const slotsResponseBody = await slotsResponse.text();
    expect(slotsResponse.status(), slotsResponseBody).toBe(200);

    await page.getByRole('combobox', { name: /Available time|Heure disponible/ }).click();
    await page.getByRole('option').first().click();
    const description = `Follow-up e2e ${Date.now()}`;
    await page.getByRole('textbox', { name: /Description/ }).fill(description);

    const createResponsePromise = page.waitForResponse(
      (response) =>
        response.request().method() === 'POST' && response.url().includes('/api/gateway/visits'),
    );
    await page.getByRole('button', { name: /Schedule|Planifier/ }).click();
    const createResponse = await createResponsePromise;
    expect(createResponse.status()).toBe(201);
    const createdVisit = (await createResponse.json()) as { visitId: string };

    await expect(
      page.getByText(/follow-up visit was scheduled|visite de suivi a été planifiée/i),
    ).toBeVisible();
    await expect(page.locator('tr').filter({ hasText: createdVisit.visitId })).toContainText(
      description,
    );

    await page.goto(`${CUSTOMER_FRONTEND_URL}/users/login`);
    await page.getByPlaceholder('Enter your email or username').fill(OWNER_EMAIL);
    await page.getByPlaceholder('Enter your password').fill(OWNER_PASSWORD);
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page).toHaveURL(`${CUSTOMER_FRONTEND_URL}/home`);
    await page.goto(`${CUSTOMER_FRONTEND_URL}/customer/visits`);

    await expect(page.getByRole('link', { name: createdVisit.visitId })).toBeVisible();
    await expect(page.locator('tr').filter({ hasText: createdVisit.visitId })).toContainText(
      description,
    );
  });
});
