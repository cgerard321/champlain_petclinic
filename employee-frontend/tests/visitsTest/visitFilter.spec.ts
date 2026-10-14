import { expect, test } from '@playwright/test';
import type { Page } from '@playwright/test';

const visitStatuses = [
  'SCHEDULED',
  'COMPLETED',
  'CANCELLED',
  'CONFIRMED',
  'UPCOMING',
  'ARCHIVED',
  'OUT_OF_STATUS',
];

async function getDisplayedStatuses(page: Page): Promise<string[]> {
  const rows = page.locator('table tr');
  const statuses: string[] = [];

  for (let rowIndex = 1; rowIndex < (await rows.count()); rowIndex++) {
    statuses.push((await rows.nth(rowIndex).locator('td').nth(6).innerText()).trim());
  }

  return statuses;
}

test('filters visits by selected statuses and preserves selection when the sidebar is toggled', async ({
  page,
}) => {
  await page.goto('http://localhost:4200/login');

  await page.getByLabel("Nom d'utilisateur ou courriel").fill('admin@admin.com');
  await page.getByLabel('Mot de passe').fill('pwd');
  await page.getByRole('button', { name: 'Se connecter' }).click();

  await expect(page).toHaveURL('http://localhost:4200/home');
  await page.goto('http://localhost:4200/vist');

  const visitRows = page.locator('table tr');
  await expect(visitRows.nth(1)).toBeVisible({ timeout: 10000 });

  // The filter menu should expose every status, selected by default.
  await page.getByRole('button', { name: 'Filtrer Par' }).click();
  const statusMenu = page.locator('#visit-status-menu');
  await expect(statusMenu).toBeVisible();
  await expect(statusMenu.locator('input[type="checkbox"]')).toHaveCount(visitStatuses.length);

  for (const status of visitStatuses) {
    await expect(statusMenu.getByLabel(status, { exact: true })).toBeChecked();
  }

  // Keep one status that is present in the table, then verify it filters the visits.
  const initialStatuses = await getDisplayedStatuses(page);
  expect(initialStatuses.length).toBeGreaterThan(0);
  const statusToKeep = initialStatuses[0];
  const secondStatusToKeep = visitStatuses.find((status) => status !== statusToKeep)!;
  const statusesToKeep = [statusToKeep, secondStatusToKeep];

  for (const status of visitStatuses) {
    if (!statusesToKeep.includes(status)) {
      await statusMenu.getByLabel(status, { exact: true }).uncheck();
    }
  }

  await expect
    .poll(async () => {
      const displayedStatuses = await getDisplayedStatuses(page);
      return (
        displayedStatuses.length > 0 &&
        displayedStatuses.every((status) => statusesToKeep.includes(status)) &&
        displayedStatuses.includes(statusToKeep)
      );
    })
    .toBe(true);

  await expect(statusMenu.getByLabel(secondStatusToKeep, { exact: true })).toBeChecked();

  // Removing the second choice should leave only visits with the first status.
  await statusMenu.getByLabel(secondStatusToKeep, { exact: true }).uncheck();
  await expect
    .poll(async () => {
      const displayedStatuses = await getDisplayedStatuses(page);
      return (
        displayedStatuses.length > 0 && displayedStatuses.every((status) => status === statusToKeep)
      );
    })
    .toBe(true);

  // Clearing the last selected status should leave only the table header.
  await statusMenu.getByLabel(statusToKeep, { exact: true }).uncheck();
  await expect(visitRows).toHaveCount(1);

  // Restore the status, hide and reopen the sidebar, and verify the choice remains selected.
  await statusMenu.getByLabel(statusToKeep, { exact: true }).check();
  await expect
    .poll(async () => {
      const displayedStatuses = await getDisplayedStatuses(page);
      return (
        displayedStatuses.length > 0 && displayedStatuses.every((status) => status === statusToKeep)
      );
    })
    .toBe(true);

  await page.getByRole('button', { name: 'Hide filters' }).click();
  await page.getByRole('button', { name: 'Afficher les filtres' }).click();
  await page.getByRole('button', { name: 'Filtrer Par' }).click();

  const reopenedStatusMenu = page.locator('#visit-status-menu');
  await expect(reopenedStatusMenu.getByLabel(statusToKeep, { exact: true })).toBeChecked();
  for (const status of visitStatuses) {
    if (status !== statusToKeep) {
      await expect(reopenedStatusMenu.getByLabel(status, { exact: true })).not.toBeChecked();
    }
  }

  const finalStatuses = await getDisplayedStatuses(page);
  expect(finalStatuses.length).toBeGreaterThan(0);
  expect(finalStatuses.every((status) => status === statusToKeep)).toBe(true);
});
