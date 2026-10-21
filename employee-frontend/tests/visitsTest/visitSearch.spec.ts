import { expect, Page, test } from '@playwright/test';

const VET_USERNAME = process.env['TEST_VET_USERNAME'] || 'vet1';
const VET_PASSWORD = process.env['TEST_VET_PASSWORD'] || 'pwd';

async function searchVisits(page: Page, query: string): Promise<void> {
  const searchResponsePromise = page.waitForResponse((response) =>
    response.url().includes('/api/gateway/visits/search'),
  );
  await page.getByLabel(/Rechercher des visites|Search visits/).fill(query);
  expect((await searchResponsePromise).status()).toBe(200);
}

test.describe('Search visits', () => {
  test('a veterinarian searches their visits by pet name, description or visit ID', async ({
    page,
  }) => {
    test.setTimeout(60_000);
    await page.goto('/login');
    await page.getByLabel("Nom d'utilisateur ou courriel").fill(VET_USERNAME);
    await page.getByLabel('Mot de passe').fill(VET_PASSWORD);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page).toHaveURL(/\/home$/, { timeout: 15000 });

    await page.goto('/vist');
    const rows = page.locator('app-visit table tr:has(td)');
    await expect(rows.first()).toBeVisible({ timeout: 10000 });
    const cells = rows.first().locator('td');
    const searches = [
      (await cells.nth(2).innerText()).trim(), // pet name
      (await cells.nth(3).innerText()).trim(), // description
      (await cells.nth(0).innerText()).trim(), // visit ID
    ];

    for (const query of searches) {
      await searchVisits(page, query);
      await expect(rows.first()).toContainText(query);
      for (const row of await rows.all()) {
        await expect(row).toContainText(query);
      }
    }

    await searchVisits(page, 'zzzz-no-visit');
    await expect(page.getByText(/Aucune visite trouvée|No visits found/)).toBeVisible();
  });
});
