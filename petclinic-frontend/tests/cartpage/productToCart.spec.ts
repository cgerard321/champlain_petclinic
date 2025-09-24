import { test, expect } from '@playwright/test';

test('add product to cart from user view and then remove it', async ({
  page,
}) => {
  //go to login page
  await page.goto('http://localhost:3000/users/login');
  await page.getByPlaceholder('Enter your email').click();
  await page.getByPlaceholder('Enter your email').fill('betty@email.com');
  await page.getByPlaceholder('Enter your password').click();
  await page.getByPlaceholder('Enter your password').fill('pwd');
  await page.getByRole('button', { name: 'Login' }).click();

  //go to shop page
  await page.getByRole('link', { name: 'Shop', exact: true }).click();

  //find the div with product that has page.getByRole('heading', { name: 'Rabbit Hutch' })
  const productDiv = page
    .getByRole('heading', { name: 'Rabbit Hutch' })
    .locator('..');

  //on the same div, find the button and click it
  await productDiv.locator('button').first().click();

  //make sure product is added to cart
  await expect(page.locator('#root')).toContainText(
    'Product added to cart successfully!'
  );

  //go to cart page
  await page
    .getByRole('link', { name: 'Shopping Cart Cart has 2 items' })
    .click();
  //make sure product is in cart with all details
  await expect(
    page.getByText(
      'Rabbit HutchOutdoor wooden hutch for rabbits$79.99RemoveAdd to Wishlist'
    )
  ).toBeVisible();

  //remove product from cart
  await page.getByLabel('Remove Rabbit Hutch from cart').click();

  //close the page
  await page.close();
});
