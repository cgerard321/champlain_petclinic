import { expect, test, type Page } from '@playwright/test';

const backendUrl = 'http://localhost:8080/api';

const staffUser = {
  username: 'admin',
  userId: 'staff-user-id',
  email: 'admin@admin.com',
  roles: ['ADMIN'],
};

const ownerUser = {
  username: 'owner',
  userId: 'owner-user-id',
  email: 'owner@example.com',
  roles: ['OWNER'],
};

const currentDate = new Date();
const currentMonth = `${currentDate.getFullYear()}-${String(
  currentDate.getMonth() + 1
).padStart(2, '0')}`;

const visits = [
  {
    visitId: 'visit-one',
    visitDate: `${currentMonth}-15T09:00:00`,
    description: 'First streamed visit',
    petId: 'pet-one',
    petName: 'Buddy',
    vetFirstName: 'Jane',
    vetLastName: 'Doe',
    vetEmail: 'jane@example.com',
    vetPhoneNumber: '555-0001',
    status: 'CONFIRMED',
    visitEndDate: `${currentMonth}-15T09:30:00`,
    isEmergency: false,
  },
  {
    visitId: 'visit-two',
    visitDate: `${currentMonth}-16T10:00:00`,
    description: 'Second streamed visit',
    petId: 'pet-two',
    petName: 'Milo',
    vetFirstName: 'John',
    vetLastName: 'Smith',
    vetEmail: 'john@example.com',
    vetPhoneNumber: '555-0002',
    status: 'UPCOMING',
    visitEndDate: `${currentMonth}-16T10:30:00`,
    isEmergency: true,
  },
];

async function mockSession(page: Page, user: typeof staffUser): Promise<void> {
  await page.route(`${backendUrl}/gateway/users/jwt`, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(user),
    });
  });

  await page.route(`${backendUrl}/gateway/pets`, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: '[]',
    });
  });
}

async function openAuthenticatedPage(
  page: Page,
  user: typeof staffUser,
  path: string
): Promise<void> {
  await mockSession(page, user);
  await page.goto(`http://localhost:3000${path}`);
  await page.waitForURL(`http://localhost:3000${path}`);
}

test.describe('Reactive visit streams', () => {
  test('renders staff visits from separate SSE events and ignores malformed events', async ({
    page,
  }) => {
    await page.route(`${backendUrl}/gateway/visits`, async route => {
      const body =
        [
          `data: ${JSON.stringify(visits[0])}`,
          '',
          'data: not-valid-json',
          '',
          `data: ${JSON.stringify(visits[1])}`,
          '',
        ].join('\n') + '\n';

      await route.fulfill({
        status: 200,
        contentType: 'text/event-stream',
        body,
      });
    });

    await openAuthenticatedPage(page, staffUser, '/visits');

    await expect(page.getByText('First streamed visit')).toBeVisible();
    await expect(page.getByText('Second streamed visit')).toBeVisible();
    await expect(page.getByText('Buddy')).toBeVisible();
    await expect(page.getByText('Milo')).toBeVisible();
  });

  test('uses the owner visit stream endpoint for customer visits', async ({
    page,
  }) => {
    let requestedUrl = '';
    await page.route(
      `${backendUrl}/gateway/visits/owners/${ownerUser.userId}/visits`,
      async route => {
        requestedUrl = route.request().url();
        await route.fulfill({
          status: 200,
          contentType: 'text/event-stream',
          body: `data: ${JSON.stringify(visits[0])}\n\n`,
        });
      }
    );

    await openAuthenticatedPage(page, ownerUser, '/customer/visits');

    await expect(page.getByText('First streamed visit')).toBeVisible();
    expect(requestedUrl).toBe(
      `${backendUrl}/gateway/visits/owners/${ownerUser.userId}/visits`
    );
  });

  test('renders visits received by the calendar SSE consumer', async ({
    page,
  }) => {
    await page.route(`${backendUrl}/gateway/visits`, async route => {
      await route.fulfill({
        status: 200,
        contentType: 'text/event-stream',
        body:
          [
            `data: ${JSON.stringify(visits[0])}`,
            '',
            `data: ${JSON.stringify(visits[1])}`,
            '',
          ].join('\n') + '\n',
      });
    });

    await openAuthenticatedPage(page, staffUser, '/visits/calendar');

    await expect(page.getByText('Buddy')).toBeVisible();
    await expect(page.getByText('Milo')).toBeVisible();
  });
});
