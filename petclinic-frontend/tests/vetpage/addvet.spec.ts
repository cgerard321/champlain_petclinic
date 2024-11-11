import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
  await page.getByPlaceholder('Enter your email').press('Tab');
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Veterinarians' }).click();
  await page.getByRole('button', { name: 'Add Vet' }).click();
  await page.getByRole('button', { name: 'Add Vet' }).click();
  await page.locator('input[name="firstName"]').click();
  await page.locator('input[name="firstName"]').press('CapsLock');
  await page.locator('input[name="firstName"]').fill('F');
  await page.locator('input[name="firstName"]').press('CapsLock');
  await page.locator('input[name="firstName"]').fill('');
  await page.locator('input[name="firstName"]').press('CapsLock');
  await page.locator('input[name="firstName"]').fill('F');
  await page.locator('input[name="firstName"]').press('CapsLock');
  await page.locator('input[name="firstName"]').fill('Felidx');
  await page.locator('input[name="firstName"]').press('Tab');
  await page.locator('input[name="lastName"]').press('CapsLock');
  await page.locator('input[name="lastName"]').fill('Z');
  await page.locator('input[name="lastName"]').press('CapsLock');
  await page.locator('input[name="lastName"]').fill('Zhang');
  await page.locator('input[name="username"]').click();
  await page.locator('input[name="username"]').fill('appdle');
  await page.locator('input[name="email"]').click();
  await page.locator('input[name="email"]').fill('apple2023119@gmail.com');
  await page.locator('input[name="email"]').press('Tab');
  await page.locator('input[name="password"]').fill('#');
  await page.locator('input[name="password"]').press('CapsLock');
  await page.locator('input[name="password"]').fill('#JS');
  await page.locator('input[name="password"]').press('CapsLock');
  await page.locator('input[name="password"]').fill('#JSs');
  await page.locator('input[name="password"]').press('CapsLock');
  await page.locator('input[name="password"]').fill('#JSsN');
  await page.locator('input[name="password"]').press('CapsLock');
  await page.locator('input[name="password"]').fill('#JSsN321j3123bjb');
  await page.locator('input[name="phoneNumber"]').click();
  await page.locator('input[name="phoneNumber"]').click({
    button: 'right',
  });
  await page.locator('input[name="phoneNumber"]').fill('(514) 718-0233 #2222');
  await page.getByText('First NameLast').click();
  await page.locator('textarea[name="resume"]').click();
  await page.locator('textarea[name="resume"]').press('CapsLock');
  await page.locator('textarea[name="resume"]').fill('I');
  await page.locator('textarea[name="resume"]').press('CapsLock');
  await page
    .getByText('I', { exact: true })
    .fill('I aajdsba da d sad sad sad a d sa');
  await page.getByRole('listbox').selectOption('2');
  await page.locator('input[name="workHoursJson"]').click();
  await page
    .locator('input[name="workHoursJson"]')
    .fill(
      '{\\"Monday\\": \\"09:00-17:00\\", \\"Wednesday\\": \\"09:00-17:00\\"}'
    );
  await page.locator('input[name="active"]').check();
  await page
    .locator('div')
    .filter({ hasText: /^Monday$/ })
    .getByRole('checkbox')
    .check();
  await page
    .locator('div')
    .filter({ hasText: /^Friday$/ })
    .getByRole('checkbox')
    .check();
  await page
    .locator('div')
    .filter({ hasText: /^Thursday$/ })
    .getByRole('checkbox')
    .check();
  await page.locator('input[name="photoDefault"]').check();
  await page
    .getByRole('dialog')
    .getByRole('button', { name: 'Add Vet' })
    .click();
});
