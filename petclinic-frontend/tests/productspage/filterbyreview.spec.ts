import { test, expect } from '@playwright/test';


test('filter by reviews', async ({ page }) => {
    await page.goto('http://localhost:3000/users/login');
    await page.getByPlaceholder('Enter your email').click();
    await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
    await page.getByPlaceholder('Enter your password').click();
    await page.getByPlaceholder('Enter your password').fill('pwd');
    await page.getByRole('button', { name: 'Login' }).click();
    await page.getByRole('link', { name: 'Shop' }).click();
    await page.getByRole('button', { name: '☰ Filters' }).click();
    await page.locator('.star-rating-container > div:nth-child(3) > .star-container > div > .partial-star > .star > path').first().click();
    await page.locator('.star-row > .star-container > div:nth-child(5) > .empty-star').first().click();
    await expect(page.locator('#sidebar')).toContainText('Minimum stars cannot be greater than or equal to maximum stars.');
    await page.close();
    
});