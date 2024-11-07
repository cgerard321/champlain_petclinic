import { test, expect } from '@playwright/test';

test.use({ storageState: 'playwright/.auth/admin.json' });

test('Admin Delete Vet', async ({ page }) => {
  // Go to the vets page.
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.waitForURL('http://localhost:3000/vets');
  const vetbox = page.locator("css=[class='card-vets']").first();
  const vet = vetbox.locator("css=[class='card-actions']");
  const deleteButton = vet.locator("css=[class='btn-danger']");
  await expect(deleteButton).toBeVisible();
});
