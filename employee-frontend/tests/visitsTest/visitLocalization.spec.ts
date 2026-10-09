import { expect, test, type Page } from '@playwright/test';

const visitStatuses = [
  { code: 'SCHEDULED', french: 'Planifiée', english: 'Scheduled' },
  { code: 'COMPLETED', french: 'Terminée', english: 'Completed' },
  { code: 'CANCELLED', french: 'Annulée', english: 'Cancelled' },
  { code: 'CONFIRMED', french: 'Confirmée', english: 'Confirmed' },
  { code: 'UPCOMING', french: 'À venir', english: 'Upcoming' },
  { code: 'ARCHIVED', french: 'Archivée', english: 'Archived' },
  { code: 'OUT_OF_STATUS', french: 'Hors statut', english: 'Out of status' },
] as const;

const tableHeaders = {
  french: [
    'Identifiant de la visite',
    'Nom complet du vétérinaire',
    "Nom de l'animal",
    'Description',
    'Date de début',
    'Date de fin',
    'Statut',
  ],
  english: [
    'Visit ID',
    'Veterinarian full name',
    'Pet name',
    'Description',
    'Start date',
    'End date',
    'Status',
  ],
};

async function expectVisitTableLanguage(page: Page, language: 'french' | 'english'): Promise<void> {
  const title = language === 'french' ? 'Liste des visites' : 'List of Visits';
  await expect(page.locator('html')).toHaveAttribute(
    'lang',
    language === 'french' ? 'fr-CA' : 'en-CA',
  );
  await expect(page.getByRole('heading', { name: title })).toBeVisible();
  await expect(page.getByRole('columnheader')).toHaveText(tableHeaders[language]);

  for (const status of visitStatuses) {
    const row = page
      .getByRole('row')
      .filter({ has: page.getByRole('cell', { name: `TEST-${status.code}`, exact: true }) });
    await expect(row.getByRole('cell').nth(6)).toHaveText(status[language]);
  }
}

test('visit page translates its table and every status when switching languages', async ({
  page,
}) => {
  const visits = visitStatuses.map((status, index) => ({
    visitId: `TEST-${status.code}`,
    visitDate: `2026-10-${String(9 - index).padStart(2, '0')}T12:00:00.000Z`,
    visitEndDate: `2026-10-${String(9 - index).padStart(2, '0')}T13:00:00.000Z`,
    description: 'Localization test visit',
    petId: `pet-${index}`,
    petName: 'Milo',
    petBirthDate: '2020-01-01T00:00:00.000Z',
    vetFirstName: 'Alex',
    vetLastName: 'Martin',
    vetEmail: 'alex@example.test',
    vetPhoneNumber: '555-0100',
    practitionerId: 'vet-test',
    status: status.code,
    isEmergency: false,
  }));

  // Provide an authenticated employee and deterministic visit stream without requiring backend services.
  await page.route('**/api/gateway/users/jwt', (route) =>
    route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        userId: 'localization-test-admin',
        email: 'admin@example.test',
        username: 'Localization Admin',
        roles: ['ADMIN'],
      }),
    }),
  );
  await page.route('**/api/gateway/visits', (route) =>
    route.fulfill({
      contentType: 'text/event-stream',
      body: visits
        .map((visit, index) => `id: ${index + 1}\ndata: ${JSON.stringify(visit)}\n\n`)
        .join(''),
    }),
  );
  await page.addInitScript(() => {
    if (!localStorage.getItem('lang')) localStorage.setItem('lang', 'fr-CA');
  });

  await page.goto('/vist');
  await expect(page.getByRole('row')).toHaveCount(visitStatuses.length + 1);
  await expectVisitTableLanguage(page, 'french');

  const englishCatalogRequest = page.waitForResponse((response) =>
    response.url().endsWith('/locale/en.json'),
  );
  const englishPageLoaded = page.waitForEvent('load');
  await page.getByRole('button', { name: 'EN' }).click();
  const catalogResponse = await englishCatalogRequest;
  await englishPageLoaded;
  expect(catalogResponse.ok()).toBeTruthy();
  await expectVisitTableLanguage(page, 'english');

  const frenchPageLoaded = page.waitForEvent('load');
  await page.getByRole('button', { name: 'FR' }).click();
  await frenchPageLoaded;
  await expectVisitTableLanguage(page, 'french');
});
