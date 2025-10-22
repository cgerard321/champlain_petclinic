import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('betty@email.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.getByRole('link', { name: 'Visits' }).click();
  await page.getByRole('button', { name: 'Leave a Review' }).click();
  await page.getByRole('spinbutton').click();
  await page.getByRole('spinbutton').fill('5');
  await page.locator('input[name="reviewerName"]').click();
  await page.locator('input[name="reviewerName"]').press('CapsLock');
  await page.locator('input[name="reviewerName"]').fill('B');
  await page.locator('input[name="reviewerName"]').press('CapsLock');
  await page.locator('input[name="reviewerName"]').fill('Betty');
  await page.locator('textarea[name="review"]').click();
  await page.locator('textarea[name="review"]').press('CapsLock');
  await page.locator('textarea[name="review"]').fill('V');
  await page.locator('textarea[name="review"]').press('CapsLock');
  await page.getByText('V', { exact: true }).fill('Very good!');
  await page.getByRole('button', { name: 'Submit Review' }).click();
});
