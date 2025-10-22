import { test, expect } from '@playwright/test'; 

test('Update Inventory', async ({ page }) => {

  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click(); 
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com'); 
  await page.getByPlaceholder('Enter your email').press('Tab'); 
  await page.getByPlaceholder('Enter your password').fill('pwd'); 
  await page.getByPlaceholder('Enter your password').press('Enter'); 
  await page.getByRole('button', { name: 'Login' }).click(); 
  await page.getByRole('link', { name: 'Inventories' }).click(); 
  await page.locator('div').filter({ hasText: /^Carlos3 Type: EquipmentMedical equipment for surgery$/ }).locator('[id="_cardMenu_cob55_1"]').click(); 
  await page.getByRole('button', { name: 'Edit' }).click(); await page.getByPlaceholder('Inventory Name').click(); 
  await page.getByPlaceholder('Inventory Name').fill(''); 
  await page.getByPlaceholder('Inventory Name').press('CapsLock'); 
  await page.getByPlaceholder('Inventory Name').fill('M'); 
  await page.getByPlaceholder('Inventory Name').press('CapsLock'); 
  await page.getByPlaceholder('Inventory Name').fill('Medical'); 
  await page.getByRole('button', { name: 'Update' }).click(); 
  await expect(page.getByText('Inventory updated successfully').first()).toBeVisible(); 
  await page.goto('http://localhost:3000/inventories'); 

});