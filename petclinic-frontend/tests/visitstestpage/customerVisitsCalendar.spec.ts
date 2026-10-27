import { test, expect, Page } from '@playwright/test';

// Uses Leo's seeded emergency visit (VIST-2203-0101) on March 1, 2022
const openMarch2022 = async (page: Page): Promise<void> => {
  await page.goto('http://localhost:3000/users/login');

  await page
    .getByPlaceholder('Enter your email or username')
    .fill('george@email.com');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  await expect(page).toHaveURL('http://localhost:3000/home');

  await page.goto('http://localhost:3000/customer/visits/calendar');

  await page.locator('.view-mode-buttons button').first().click();

  const yearTitle = page.locator('.current-period h3');
  await expect(yearTitle).toHaveText(/\d{4}/);
  while ((await yearTitle.innerText()) !== '2022') {
    await page.locator('.nav-button').first().click();
  }

  await page.locator('.year-month-cell').nth(2).click();
};

test('calendar labels visits and shows them in a day pop-up', async ({
  page,
}) => {
  await openMarch2022(page);

  await expect(page.locator('.current-period h3')).toHaveText('March 2022');

  const label = page.locator('.customer-visit-label').first();
  await expect(label).toContainText('13:00');
  await expect(label).toContainText('Leo');
  await expect(label).toContainText('Completed');
  await expect(label.locator('.calendar-emergency-badge')).toHaveText(
    'Emergency'
  );
  await expect(label).toHaveClass(/status-completed/);

  const legend = page.locator('.calendar-legend');
  for (const status of [
    'Upcoming',
    'Confirmed',
    'Completed',
    'Cancelled',
    'Archived',
    'Emergency',
  ]) {
    await expect(legend).toContainText(status);
  }

  await label.click();

  const modal = page.locator('.customer-day-modal');
  await expect(modal).toBeVisible();
  await expect(modal).toContainText('Tuesday, March 1, 2022');
  await expect(modal).toContainText('13:00 - 14:00');
  await expect(modal).toContainText('Leo');
  await expect(modal).toContainText('Dr. Helen Leary');
  await expect(modal).toContainText('Dog Needs Meds');
  await expect(modal.locator('.calendar-emergency-badge')).toBeVisible();

  await modal.locator('.btn-close').click();
  await expect(modal).toBeHidden();
});

test('clicking a visit in the pop-up opens its details page', async ({
  page,
}) => {
  await openMarch2022(page);

  await page.locator('.customer-visit-label').first().click();
  await page.locator('.customer-day-modal .week-visit-item').first().click();

  await expect(page).toHaveURL('http://localhost:3000/visits/VIST-2203-0101');
});

test('calendar is translated to French', async ({ page }) => {
  await openMarch2022(page);

  await page.getByRole('button', { name: 'FR' }).click();

  await expect(page.locator('.calendar-title h2')).toHaveText(
    'Mes rendez-vous'
  );
  await expect(page.locator('.current-period h3')).toHaveText('mars 2022');

  const label = page.locator('.customer-visit-label').first();
  await expect(label).toContainText('Terminée');
  await expect(label.locator('.calendar-emergency-badge')).toHaveText(
    'Urgence'
  );
  await expect(page.locator('.calendar-legend')).toContainText('Archivée');
});
