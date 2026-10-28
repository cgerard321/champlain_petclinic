import { expect, test, type Page } from '@playwright/test';

const backendUrl = 'http://localhost:8080/api';

const customerUser = {
  username: 'customer',
  userId: 'customer-user-id',
  email: 'customer@example.com',
  roles: ['CUST'],
};

const customerBill = {
  billId: 'bill-1',
  customerId: customerUser.userId,
  customerFirstName: 'Casey',
  customerLastName: 'Customer',
  visitType: 'Annual check-up',
  vetId: 'vet-1',
  vetFirstName: 'Jamie',
  vetLastName: 'Vet',
  date: '2026-10-20',
  amount: 100,
  taxedAmount: 114.98,
  gstAmount: 5,
  qstAmount: 9.98,
  interest: 0,
  totalAmount: 114.98,
  billStatus: 'UNPAID',
  dueDate: '2026-11-20',
  timeRemaining: 25,
  interestExempt: false,
  archive: false,
};

async function mockCustomerSession(page: Page): Promise<void> {
  await page.route(`${backendUrl}/gateway/users/jwt`, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(customerUser),
    });
  });

  await page.route(
    `${backendUrl}/gateway/customers/${customerUser.userId}/cart`,
    async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ cartId: null }),
      });
    }
  );

  await page.route(
    `${backendUrl}/v2/gateway/customers/${customerUser.userId}/bills`,
    async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([customerBill]),
      });
    }
  );

  await page.route(
    `${backendUrl}/v2/gateway/customers/${customerUser.userId}/bills/current-balance`,
    async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: '114.98',
      });
    }
  );
}

test.describe('Customer billing access', () => {
  test('does not expose administrator billing controls', async ({ page }) => {
    await mockCustomerSession(page);
    await page.goto('http://localhost:3000/bills/customer');

    await expect(
      page.getByRole('heading', { name: 'Your Bills' })
    ).toBeVisible();
    await expect(page.getByText('Jamie Vet')).toBeVisible();

    await expect(page.getByRole('button', { name: 'Details' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Create' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Update' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Archive' })).toHaveCount(0);
    await expect(
      page.getByRole('button', { name: /Show Archived Bills/i })
    ).toHaveCount(0);
    await expect(
      page.getByRole('button', { name: 'Exempt Interest' })
    ).toHaveCount(0);
  });

  test('blocks customer direct navigation to administrator billing', async ({
    page,
  }) => {
    await mockCustomerSession(page);
    await page.goto('http://localhost:3000/bills/admin');

    await page.waitForURL('http://localhost:3000/forbidden');
    await expect(page.getByRole('heading', { name: '403' })).toBeVisible();
    await expect(
      page.getByText(
        'Sorry, you do not have permission to access this page or resource.'
      )
    ).toBeVisible();
  });
});
