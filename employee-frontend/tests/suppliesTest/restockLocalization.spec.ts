import { expect, test } from '@playwright/test';

for (const lang of ['en', 'fr']) {
  for (const mobile of [false, true]) {
    test(`restock localization ${lang} ${mobile ? 'mobile' : 'desktop'}`, async ({ page }) => {
      await page.setViewportSize({ width: mobile ? 390 : 1440, height: 900 });
      await page.addInitScript((language) => localStorage.setItem('lang', language), lang);
      const supply = {
        productId: 'low-stock',
        inventoryId: 'inventory',
        productName: 'Custom bandages',
        productDescription: '',
        productQuantity: 0,
        productPrice: 5,
        productSalePrice: 10,
        status: 'OUT_OF_STOCK',
        lastUpdatedAt: '',
      };
      await page.route('**/api/gateway/**', async (route) => {
        if (route.request().url().includes('/users/jwt')) {
          await route.fulfill({
            json: { roles: ['INVENTORY_MANAGER'], username: 'Inventory', userId: 'test' },
          });
        } else if (route.request().url().endsWith('/products')) {
          await route.fulfill({
            contentType: 'text/event-stream',
            body: `data: ${JSON.stringify(supply)}\n\n`,
          });
        } else if (route.request().url().includes('/restockProduct')) {
          await new Promise((resolve) => setTimeout(resolve, 500));
          await route.fulfill({ status: 500, json: { message: 'Server failure' } });
        } else {
          await route.fulfill({ json: [] });
        }
      });
      await page.goto('/invt/inventory/supplies');
      if (mobile) await page.locator('.mobile-actions-button').click();
      const label = lang === 'en' ? 'Restock' : 'R\u00e9approvisionner';
      const action = page.getByRole(mobile ? 'menuitem' : 'button', { name: label, exact: true });
      await expect(action).toBeVisible();
      await action.click();
      const dialog = page.getByRole('dialog');
      await expect(dialog).toHaveAccessibleName(`${label} Custom bandages`);
      await expect(dialog.getByRole('heading')).toHaveText(
        lang === 'en' ? 'Restock supply' : 'R\u00e9approvisionner un article',
      );
      await expect(dialog).toContainText(
        lang === 'en'
          ? 'Custom bandages \u2014 Current quantity: 0'
          : 'Custom bandages \u2014 Quantit\u00e9 actuelle : 0',
      );
      const quantity = dialog.getByLabel(
        lang === 'en' ? 'Quantity to add' : 'Quantit\u00e9 \u00e0 ajouter',
      );
      await expect(quantity).toBeVisible();
      await expect(
        dialog.getByRole('button', { name: lang === 'en' ? 'Cancel' : 'Annuler', exact: true }),
      ).toBeVisible();
      await quantity.fill('0');
      await quantity.blur();
      await expect(dialog).toContainText(
        lang === 'en'
          ? 'Enter an integer greater than 0.'
          : 'Saisissez un entier sup\u00e9rieur \u00e0 0.',
      );
      await quantity.fill('3');
      await dialog
        .getByRole('button', {
          name: lang === 'en' ? 'Add stock' : 'Ajouter au stock',
          exact: true,
        })
        .click();
      await expect(
        dialog.getByRole('button', {
          name: lang === 'en' ? 'Adding stock...' : 'Ajout en cours\u2026',
          exact: true,
        }),
      ).toBeDisabled();
      await expect(dialog.getByRole('alert')).toContainText(
        lang === 'en'
          ? 'Unable to confirm restock. Stock may have changed.'
          : 'Impossible de confirmer le r\u00e9approvisionnement. Le stock a peut-\u00eatre chang\u00e9.',
      );
      await expect(quantity).toHaveValue('3');
      await expect(
        dialog.getByRole('button', {
          name: lang === 'en' ? 'Add stock' : 'Ajouter au stock',
          exact: true,
        }),
      ).toBeDisabled();
    });
  }
}
