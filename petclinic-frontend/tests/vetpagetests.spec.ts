import { test, expect } from '@playwright/test';

test.describe(() => {
  test.use({ storageState: '.auth/admin.json' });

  test('Admin Delete Vet', async ({ page }) => {
    await page.goto('http://localhost:3000/vets');
    const vetCard = page
      .locator('.card-vets')
      .filter({ hasText: 'James Carter' });
    await expect(vetCard).toBeVisible();
    const vetCardButton = vetCard.locator(
      '.card-content > .card-actions > .btn'
    );
    await expect(vetCardButton).toBeVisible();
    await vetCardButton.click();
    await page.waitForTimeout(3000);
    
    // await expect(
    //   page.locator('div').filter({ hasText: 'James CarterSpecialties:' }).nth(3)
    // ).toBeVisible();
    // await expect(
    //   page.locator('div:nth-child(3) > .card-content > .card-actions > .btn')
    // ).toBeVisible();
    // await expect(page.locator('#root')).toContainText('Delete');
    // await page
    //   .locator('div:nth-child(3) > .card-content > .card-actions > .btn')
    //   .click();
    page.once('dialog', dialog => {
      //console.log(`Dialog message: ${dialog.message()}`);
      dialog.dismiss().catch(() => {});
    });
    await expect(
      page.locator('.card-vets').filter({ hasText: 'James Carter' })
    ).not.toBeVisible();
  });
});
