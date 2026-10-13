import { expect, test } from '@playwright/test';

const VET_USERNAME = process.env['TEST_VET_USERNAME'] || 'vet3';
const VET_PASSWORD = process.env['TEST_VET_PASSWORD'] || 'pwd';

const VISIT_STATUSES = [
  'SCHEDULED',
  'COMPLETED',
  'CANCELLED',
  'CONFIRMED',
  'UPCOMING',
  'ARCHIVED',
  'OUT_OF_STATUS',
] as const;

const FRENCH_HEADERS = [
  'Identifiant de la visite',
  'Nom complet du vétérinaire',
  "Nom de l'animal",
  'Description',
  'Date de début',
  'Date de fin',
  'Statut',
];

const ENGLISH_HEADERS = [
  'Visit ID',
  'Veterinarian full name',
  'Pet name',
  'Description',
  'Start date',
  'End date',
  'Status',
];

const FRENCH_STATUSES = [
  'Planifiée',
  'Complétée',
  'Annulée',
  'Confirmée',
  'À venir',
  'Archivée',
  'Hors statut',
];

const ENGLISH_STATUSES = [
  'Scheduled',
  'Completed',
  'Cancelled',
  'Confirmed',
  'Upcoming',
  'Archived',
  'Out of status',
];

const VISITS = VISIT_STATUSES.map((status, index) => ({
  visitId: `VIST-LOCALIZATION-${index}`,
  visitDate: '2026-10-13T14:30:00.000Z',
  visitEndDate: '2026-10-13T15:30:00.000Z',
  description: `${status} localization sample`,
  petId: `pet-${index}`,
  petName: status === 'COMPLETED' ? 'Leo' : `Pet ${index}`,
  petBirthDate: '2020-01-01T00:00:00.000Z',
  vetFirstName: 'Linda',
  vetLastName: 'Douglas',
  vetEmail: 'linda@example.com',
  vetPhoneNumber: '555-1234',
  practitionerId: 'vet-localization',
  status,
  isEmergency: false,
}));

test('renders the visit list in French and English with localized statuses and dates', async ({
  page,
}) => {
  test.setTimeout(60_000);

  await page.goto('/login');
  await page.evaluate(() => localStorage.setItem('lang', 'fr-CA'));
  await page.reload();

  await page.getByLabel("Nom d'utilisateur ou courriel").fill(VET_USERNAME);
  await page.getByLabel('Mot de passe').fill(VET_PASSWORD);
  await page.getByRole('button', { name: 'Se connecter' }).click();
  await expect(page).toHaveURL(/\/home$/);

  const frontendOrigin = new URL(page.url()).origin;
  await page.route('**/api/gateway/visits/vets/*/visits', (route) =>
    route.fulfill({
      status: 200,
      headers: {
        'Content-Type': 'text/event-stream',
        'Access-Control-Allow-Origin': frontendOrigin,
        'Access-Control-Allow-Credentials': 'true',
      },
      body: VISITS.map((visit) => `data: ${JSON.stringify(visit)}\n\n`).join(''),
    }),
  );

  await page.goto('/vist');
  await expect(page.getByRole('heading', { name: 'Liste des visites' })).toBeVisible();
  for (const header of FRENCH_HEADERS) {
    await expect(page.getByRole('columnheader', { name: header, exact: true })).toBeVisible();
  }
  for (const status of FRENCH_STATUSES) {
    await expect(page.getByRole('cell', { name: status, exact: true })).toBeVisible();
  }

  const frenchCompletedVisit = page
    .locator('table tr')
    .filter({ hasText: 'Leo' })
    .filter({ hasText: 'Complétée' })
    .first();
  await expect(frenchCompletedVisit).toBeVisible();
  const frenchDate = (await frenchCompletedVisit.locator('td').nth(4).innerText()).trim();
  expect(frenchDate).not.toContain('T');

  const englishCatalogueResponse = page.waitForResponse((response) =>
    response.url().endsWith('/locale/en.json'),
  );
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  expect((await englishCatalogueResponse).ok()).toBe(true);

  await expect(page.locator('html')).toHaveAttribute('lang', 'en-CA');
  await expect(page.getByRole('heading', { name: 'List of Visits' })).toBeVisible();
  for (const header of ENGLISH_HEADERS) {
    await expect(page.getByRole('columnheader', { name: header, exact: true })).toBeVisible();
  }
  for (const status of ENGLISH_STATUSES) {
    await expect(page.getByRole('cell', { name: status, exact: true })).toBeVisible();
  }

  const englishCompletedVisit = page
    .locator('table tr')
    .filter({ hasText: 'Leo' })
    .filter({ hasText: 'Completed' })
    .first();
  await expect(englishCompletedVisit).toBeVisible();
  const englishDate = (await englishCompletedVisit.locator('td').nth(4).innerText()).trim();
  expect(englishDate).not.toBe(frenchDate);
});
