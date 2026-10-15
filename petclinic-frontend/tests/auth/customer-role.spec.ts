import { expect, test, type Page } from '@playwright/test';

const origin = 'http://localhost:3000';
const api = 'http://localhost:8080/api';
const userId = 'customer-role-test-user';
const customer = {
  customerId: userId,
  firstName: 'Test',
  lastName: 'Customer',
  address: '123 Test Street',
  city: 'Montreal',
  province: 'Quebec',
  telephone: '5141234567',
  pets: [],
};

async function mockApi(page: Page, roles: string[]) {
  const state = { roles, requests: [] as string[], sessionStatus: 200 };
  await page.route(`${api}/**`, async route => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    state.requests.push(`${request.method()} ${path}`);
    const session = {
      userId,
      username: 'role-test-user',
      email: 'role-test@example.com',
      roles: state.roles,
    };
    const user = {
      ...session,
      verified: true,
      roles: state.roles.map((name, id) => ({ id: id + 42, name })),
    };
    let body: unknown = [];
    if (path.endsWith('/users/jwt')) {
      await route.fulfill({ status: state.sessionStatus, json: session });
      return;
    }
    if (path.endsWith('/users/login')) {
      state.roles = ['CUST'];
      body = session;
    } else if (path.endsWith(`/users/${userId}`)) {
      body = user;
    } else if (path.endsWith('/users')) {
      body = [user];
    } else if (path.endsWith('/roles')) {
      body = [
        { id: 42, name: 'CUST' },
        { id: 91, name: 'ADMIN' },
      ];
    } else if (path.endsWith(`/customers/${userId}/cart`)) {
      body = { cartId: 'role-test-cart' };
    } else if (path.endsWith('/carts/role-test-cart')) {
      body = {
        cartId: 'role-test-cart',
        customerId: userId,
        products: [{ productId: 'product-one', quantityInCart: 3 }],
      };
    } else if (path.endsWith(`/customers/${userId}`)) {
      body = customer;
    } else if (path.endsWith('/pets/pet-one')) {
      body = {
        petId: 'pet-one',
        ownerId: userId,
        name: 'Test Pet',
        petTypeId: 'cat',
        birthDate: '2020-01-01',
        weight: 4,
      };
    }
    await route.fulfill({ status: 200, json: body });
  });
  return state;
}

test.describe('CUST authentication role', () => {
  for (const path of [
    '/customer/profile',
    '/customer/profile/edit',
    '/customer/visits/calendar',
    '/owner/book-appointment',
    '/pets/pet-one/edit',
  ]) {
    test(`CUST retains access to ${path}`, async ({ page }) => {
      await mockApi(page, ['CUST']);
      await page.goto(origin + path);

      await expect(page.locator('#user-dropdown')).toHaveText('role-test-user');
      await expect(page.locator('h1, h2').first()).toBeVisible();
      await expect(page).toHaveURL(origin + path);
      await expect(
        page.getByRole('heading', { name: '403', exact: true })
      ).toHaveCount(0);
    });
  }

  for (const role of ['ADMIN', 'VET']) {
    test(`${role} retains access to the shared pet update route`, async ({
      page,
    }) => {
      await mockApi(page, [role]);
      await page.goto(`${origin}/pets/pet-one/edit`);

      await expect(page.locator('#user-dropdown')).toHaveText('role-test-user');
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
      await expect(page).toHaveURL(`${origin}/pets/pet-one/edit`);
    });
  }

  for (const role of ['ADMIN', 'VET', 'RECEPTIONIST', 'INVENTORY_MANAGER']) {
    test(`${role} remains denied on the customer-only profile route`, async ({
      page,
    }) => {
      await mockApi(page, [role]);
      await page.goto(`${origin}/customer/profile`);

      await expect(page).toHaveURL(`${origin}/forbidden`);
      await expect(
        page.getByRole('heading', { name: '403', exact: true })
      ).toBeVisible();
    });
  }

  test('CUST remains denied on the admin users route', async ({ page }) => {
    await mockApi(page, ['CUST']);
    await page.goto(`${origin}/users`);

    await expect(page).toHaveURL(`${origin}/forbidden`);
    await expect(
      page.getByRole('heading', { name: '403', exact: true })
    ).toBeVisible();
  });

  test('CUST loads its cart and renders customer navigation', async ({
    page,
  }) => {
    const state = await mockApi(page, ['CUST']);
    await page.goto(`${origin}/home`);

    await expect(page.getByLabel('Cart has 3 items')).toBeVisible();
    expect(state.requests).toContain(
      `GET /api/gateway/customers/${userId}/cart`
    );
    expect(state.requests).toContain('GET /api/gateway/carts/role-test-cart');
    await page.locator('#user-dropdown').click();
    await expect(
      page.getByRole('link', { name: 'Profile', exact: true })
    ).toBeVisible();
  });

  for (const role of [
    'ADMIN',
    'VET',
    'RECEPTIONIST',
    'INVENTORY_MANAGER',
    'OWNER',
  ]) {
    test(`${role} does not receive customer cart behavior`, async ({
      page,
    }) => {
      const state = await mockApi(page, [role]);
      await page.goto(`${origin}/home`);

      await expect(page.locator('#user-dropdown')).toHaveText('role-test-user');
      await expect(page.getByLabel('Shopping Cart')).toHaveCount(0);
      expect(
        state.requests.filter(request => request.includes('/cart'))
      ).toHaveLength(0);
      await page.locator('#user-dropdown').click();
      await expect(
        page.getByRole('link', { name: 'Profile', exact: true })
      ).toHaveCount(0);
    });
  }

  test('Customer labels keep CUST selected and submit its role code', async ({
    page,
  }) => {
    await mockApi(page, ['ADMIN', 'CUST']);
    await page.goto(`${origin}/users/${userId}`);

    await expect(page.getByText('Roles: ADMIN, Customer')).toBeVisible();
    await expect(
      page.getByRole('button', { name: 'Connected Customer Info' })
    ).toBeVisible();
    await page.getByRole('button', { name: 'Update User Roles' }).click();
    await expect(
      page.getByRole('checkbox', { name: 'Customer', exact: true })
    ).toBeChecked();
    await expect(
      page.getByRole('checkbox', { name: 'ADMIN', exact: true })
    ).toBeChecked();
    await page.getByRole('checkbox', { name: 'ADMIN', exact: true }).uncheck();
    const requestPromise = page.waitForRequest(
      request =>
        request.method() === 'PATCH' &&
        request.url().endsWith(`/users/${userId}`)
    );
    const dialogPromise = page.waitForEvent('dialog');
    await page.getByRole('button', { name: 'Save', exact: true }).click();
    const request = await requestPromise;
    expect(request.postDataJSON()).toStrictEqual({ roles: ['CUST'] });
    const dialog = await dialogPromise;
    expect(dialog.message()).toBe('User roles updated successfully.');
    await Promise.all([page.waitForEvent('load'), dialog.accept()]);
    await expect(page.getByText('Roles: ADMIN, Customer')).toBeVisible();
  });

  test('another role keeps its label and has no connected customer action', async ({
    page,
  }) => {
    await mockApi(page, ['ADMIN']);
    await page.goto(`${origin}/users/${userId}`);

    await expect(page.getByText('Roles: ADMIN', { exact: true })).toBeVisible();
    await expect(
      page.getByRole('button', { name: 'Connected Customer Info' })
    ).toHaveCount(0);
  });

  test('a valid old OWNER session is denied customer access without an alias', async ({
    page,
  }) => {
    await mockApi(page, ['OWNER']);
    await page.goto(`${origin}/customer/profile`);

    await expect(page).toHaveURL(`${origin}/forbidden`);
    await expect(
      page.getByRole('heading', { name: '403', exact: true })
    ).toBeVisible();
  });

  test('fresh login reloads CUST from the authoritative session response', async ({
    page,
  }) => {
    const state = await mockApi(page, ['OWNER']);
    await page.goto(`${origin}/users/login`);
    await page
      .getByPlaceholder('Enter your email or username')
      .fill('role-test-user');
    await page
      .getByPlaceholder('Enter your password', { exact: true })
      .fill('test-password');
    await page.getByRole('button', { name: 'Login', exact: true }).click();

    await expect(page).toHaveURL(`${origin}/home`);
    await expect(page.getByLabel('Cart has 3 items')).toBeVisible();
    expect(state.roles).toStrictEqual(['CUST']);
    await page.locator('#user-dropdown').click();
    await page.getByRole('link', { name: 'Profile', exact: true }).click();
    await expect(page).toHaveURL(`${origin}/customer/profile`);
  });

  test('failed session validation keeps the existing login redirect', async ({
    page,
  }) => {
    const state = await mockApi(page, ['CUST']);
    state.sessionStatus = 401;
    await page.goto(`${origin}/customer/profile`);

    await expect(page).toHaveURL(`${origin}/users/login`);
    await expect(
      page.getByRole('button', { name: 'Login', exact: true })
    ).toBeVisible();
  });
});
