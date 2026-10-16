import { expect, Page, test } from '@playwright/test';

const VET_USERNAME = process.env['TEST_VET_USERNAME'] || 'vet3';
const VET_PASSWORD = process.env['TEST_VET_PASSWORD'] || 'pwd';
const OWNER_EMAIL = process.env['TEST_OWNER_EMAIL'] || 'george@email.com';
const OTHER_OWNER_EMAIL = process.env['TEST_OTHER_OWNER_EMAIL'] || 'betty@email.com';
const OWNER_PASSWORD = process.env['TEST_OWNER_PASSWORD'] || 'pwd';
const CUSTOMER_FRONTEND_URL = process.env['CUSTOMER_FRONTEND_URL'] || 'http://localhost:3000';
async function openVisitAsOwner(page: Page, email: string, visitId: string): Promise<void> {
  await page.goto(`${CUSTOMER_FRONTEND_URL}/users/login`);
  await page.getByPlaceholder('Enter your email or username').fill(email);
  await page.getByPlaceholder('Enter your password').fill(OWNER_PASSWORD);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(`${CUSTOMER_FRONTEND_URL}/home`);
  await page.goto(`${CUSTOMER_FRONTEND_URL}/visits/${visitId}`);
  await expect(page.getByText(visitId)).toBeVisible();
}

test.describe('Leave a note on a visit', () => {
  test('a veterinarian adds a note that only the pet owner can see', async ({ page, browser }) => {
    test.setTimeout(60_000);
    await page.goto('/login');
    await page.getByLabel("Nom d'utilisateur ou courriel").fill(VET_USERNAME);
    await page.getByLabel('Mot de passe').fill(VET_PASSWORD);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page).toHaveURL(/\/home$/);

    await page.goto('/vist');
    const completedVisit = page
      .locator('tr')
      .filter({ hasText: 'Leo' })
      .filter({ hasText: /Complétée|Completed/ })
      .first();
    const visitId = (await completedVisit.locator('td').first().textContent())!.trim();
    await completedVisit.getByRole('button', { name: /Ajouter une note|Add note/ }).click();

    const noteField = page.getByRole('textbox', { name: /Note pour le propriétaire|Note for/ });
    await noteField.fill('   ');
    await page.getByRole('button', { name: /Enregistrer|Save/ }).click();
    await expect(
      page.getByText(
        /La note doit contenir au moins 5 caractères|Note must be at least 5 characters/,
      ),
    ).toBeVisible();

    const note = `Keep giving the meds ${Date.now()}`;
    await noteField.fill(note);
    const saveResponsePromise = page.waitForResponse(
      (response) =>
        response.request().method() === 'PATCH' &&
        response.url().includes(`/api/gateway/visits/${visitId}/note`),
    );
    await page.getByRole('button', { name: /Enregistrer|Save/ }).click();
    expect((await saveResponsePromise).status()).toBe(200);
    await expect(page.getByText(/La note a été enregistrée|The note was saved/)).toBeVisible();

    await openVisitAsOwner(page, OWNER_EMAIL, visitId);
    await expect(page.getByText(note)).toBeVisible();

    const otherOwnerPage = await (await browser.newContext()).newPage();
    await openVisitAsOwner(otherOwnerPage, OTHER_OWNER_EMAIL, visitId);
    await expect(otherOwnerPage.getByText(note)).toHaveCount(0);
  });
});
