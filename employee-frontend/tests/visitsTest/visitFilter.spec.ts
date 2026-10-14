import { expect, test, type Page } from '@playwright/test';

const visitStatuses = [
  { code: 'SCHEDULED', french: 'Planifiée', english: 'Scheduled' },
  { code: 'COMPLETED', french: 'Complétée', english: 'Completed' },
  { code: 'CANCELLED', french: 'Annulée', english: 'Cancelled' },
  { code: 'CONFIRMED', french: 'Confirmée', english: 'Confirmed' },
  { code: 'UPCOMING', french: 'À venir', english: 'Upcoming' },
  { code: 'ARCHIVED', french: 'Archivée', english: 'Archived' },
  { code: 'OUT_OF_STATUS', french: 'Hors statut', english: 'Out of status' },
] as const;

type Language = 'french' | 'english';

async function getDisplayedStatuses(page: Page): Promise<string[]> {
  const rows = page.locator('table tr');
  const statuses: string[] = [];

  for (let rowIndex = 1; rowIndex < (await rows.count()); rowIndex++) {
    statuses.push((await rows.nth(rowIndex).locator('td').nth(6).innerText()).trim());
  }

  return statuses;
}

async function openVisitsPage(page: Page): Promise<void> {
  const visits = visitStatuses.map((status, index) => ({
    visitId: `FILTER-${status.code}`,
    visitDate: `2026-10-${String(9 - index).padStart(2, '0')}T12:00:00.000Z`,
    visitEndDate: `2026-10-${String(9 - index).padStart(2, '0')}T13:00:00.000Z`,
    description: 'Filter test visit',
    petId: `filter-pet-${index}`,
    petName: 'Milo',
    petBirthDate: '2020-01-01T00:00:00.000Z',
    vetFirstName: 'Alex',
    vetLastName: 'Martin',
    vetEmail: 'alex@example.test',
    vetPhoneNumber: '555-0100',
    practitionerId: 'filter-vet',
    status: status.code,
    isEmergency: false,
  }));

  await page.route('**/api/gateway/users/jwt', (route) =>
    route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        userId: 'filter-test-admin',
        email: 'admin@example.test',
        username: 'Filter Test Admin',
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
    localStorage.setItem('lang', 'fr-CA');
  });

  await page.goto('/vist');
  await expect(page.getByRole('row')).toHaveCount(visitStatuses.length + 1);
}

async function expectAllStatusesChecked(page: Page, language: Language): Promise<void> {
  const menu = page.locator('#visit-status-menu');
  await expect(menu).toBeVisible();

  for (const status of visitStatuses) {
    await expect(menu.getByLabel(status[language], { exact: true })).toBeChecked();
  }
}

test('opens and closes the status menu with every localized visit status selected', async ({
  page,
}) => {
  await openVisitsPage(page);

  const filterButton = page.getByRole('button', { name: 'Filtrer Par' });
  const menu = page.locator('#visit-status-menu');

  await expect(filterButton).toHaveAttribute('aria-expanded', 'false');
  await expect(menu).toHaveCount(0);
  await filterButton.click();
  await expect(filterButton).toHaveAttribute('aria-expanded', 'true');
  await expect(menu.locator('input[type="checkbox"]')).toHaveCount(visitStatuses.length);
  await expectAllStatusesChecked(page, 'french');

  await filterButton.click();
  await expect(filterButton).toHaveAttribute('aria-expanded', 'false');
  await expect(menu).toHaveCount(0);

  await filterButton.click();
  await expectAllStatusesChecked(page, 'french');
});

test('filters visits, preserves choices when the sidebar is toggled, and translates filter controls', async ({
  page,
}) => {
  await openVisitsPage(page);

  await page.getByRole('button', { name: 'Filtrer Par' }).click();
  const menu = page.locator('#visit-status-menu');
  await expectAllStatusesChecked(page, 'french');

  const statusesToKeep = ['SCHEDULED', 'UPCOMING'];
  for (const status of visitStatuses) {
    if (!statusesToKeep.includes(status.code)) {
      await menu.getByLabel(status.french, { exact: true }).uncheck();
    }
  }

  await expect
    .poll(async () => (await getDisplayedStatuses(page)).sort())
    .toEqual(['À venir', 'Planifiée'].sort());

  await menu.getByLabel('Planifiée', { exact: true }).uncheck();
  await expect(page.getByRole('row')).toHaveCount(2);
  await expect(page.getByRole('cell', { name: 'À venir', exact: true })).toHaveCount(1);

  await menu.getByLabel('À venir', { exact: true }).uncheck();
  await expect(page.getByRole('row')).toHaveCount(1);

  await menu.getByLabel('Complétée', { exact: true }).check();
  await expect(page.getByRole('row')).toHaveCount(2);
  await expect(page.getByRole('cell', { name: 'Complétée', exact: true })).toHaveCount(1);

  await page.getByRole('button', { name: 'Hide filters' }).click();
  const showFilters = page.getByRole('button', { name: 'Afficher les filtres' });
  await expect(showFilters).toBeVisible();
  await showFilters.click();

  const filterButton = page.getByRole('button', { name: 'Filtrer Par' });
  await filterButton.click();
  await expect(
    page.locator('#visit-status-menu').getByLabel('Complétée', { exact: true }),
  ).toBeChecked();
  for (const status of visitStatuses) {
    if (status.code !== 'COMPLETED') {
      await expect(
        page.locator('#visit-status-menu').getByLabel(status.french, { exact: true }),
      ).not.toBeChecked();
    }
  }
  await expect.poll(() => getDisplayedStatuses(page)).toEqual(['Complétée']);

  const englishCatalogRequest = page.waitForResponse((response) =>
    response.url().endsWith('/locale/en.json'),
  );
  await page.getByRole('button', { name: 'EN', exact: true }).click();
  const englishCatalogResponse = await englishCatalogRequest;
  expect(englishCatalogResponse.ok()).toBe(true);

  const englishFilterButton = page.getByRole('button', { name: 'Filter By' });
  await expect(englishFilterButton).toBeVisible();
  await englishFilterButton.click();
  const englishMenu = page.locator('#visit-status-menu');
  await expectAllStatusesChecked(page, 'english');
  await expect(englishMenu.getByLabel('Completed', { exact: true })).toBeChecked();

  await englishFilterButton.click();
  await page.getByRole('button', { name: 'Hide filters' }).click();
  await expect(page.getByRole('button', { name: 'Show filters' })).toBeVisible();
});
