
import { expect, test, type Page } from '@playwright/test';

const FRONTEND = 'http://localhost:3000';
const API = 'http://localhost:8080/api/gateway';
const REVIEW_PAGE = '/customers/visits/reviews';
const ADD_REVIEW_PAGE = '/customers/visits/reviews/add';
const REQUIREMENT =
    'You must have at least one completed appointment before you can leave a review.';

const customer = {
    username: 'owner',
    userId: 'customer-1',
    email: 'owner@example.com',
    roles: ['CUST'],
};

async function mockAuthenticatedCustomer(page: Page): Promise<void> {
    await page.route(`${API}/users/jwt`, async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(customer),
        });
    });

    await page.route(`${API}/visits/reviews`, async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: '[]',
        });
    });

    await page.route(`${API}/customers/${customer.userId}**`, async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                customerId: customer.userId,
                firstName: 'Jane',
                lastName: 'Doe',
            }),
        });
    });
}

async function mockEligibility(
    page: Page,
    eligible: boolean
): Promise<void> {
    await page.route(`${API}/visits/reviews/eligibility`, async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ eligible }),
        });
    });
}

test.describe('Customer review eligibility', () => {
    test('ineligible customer sees a gray disabled Add Review button and explanation', async ({ page }) => {
        await mockAuthenticatedCustomer(page);
        await mockEligibility(page, false);

        await page.goto(`${FRONTEND}${REVIEW_PAGE}`);

        const addReview = page.getByRole('button', { name: 'Add Review' });
        await expect(addReview).toBeDisabled();
        await expect(addReview).toHaveClass(/btn-secondary/);
        await expect(page.getByText(REQUIREMENT)).toBeVisible();

        // A disabled trigger cannot open the modal.
        await expect(page.locator('#review-form')).toHaveCount(0);
    });

    test('eligible customer can open Add Review and sees no requirement message', async ({ page }) => {
        await mockAuthenticatedCustomer(page);
        await mockEligibility(page, true);

        await page.goto(`${FRONTEND}${REVIEW_PAGE}`);

        const addReview = page.getByRole('button', { name: 'Add Review' });
        await expect(addReview).toBeEnabled();
        await expect(addReview).toHaveClass(/btn-primary/);
        await expect(page.getByText(REQUIREMENT)).toHaveCount(0);

        await addReview.click();
        await expect(page.locator('#review-form')).toBeVisible();
    });

    test('ineligible customer cannot access the standalone review form directly', async ({ page }) => {
        await mockAuthenticatedCustomer(page);
        await mockEligibility(page, false);

        await page.goto(`${FRONTEND}${ADD_REVIEW_PAGE}`);

        await expect(page.getByText(REQUIREMENT)).toBeVisible();
        await expect(page.locator('.add-review-form form')).toHaveCount(0);
        await expect(page.getByRole('button', { name: 'Submit' })).toHaveCount(0);
    });

    test('reloading the Reviews page rechecks eligibility and enables the button', async ({ page }) => {
        await mockAuthenticatedCustomer(page);

        let eligible = false;
        let requests = 0;
        await page.route(`${API}/visits/reviews/eligibility`, async route => {
            requests += 1;
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({ eligible }),
            });
        });

        await page.goto(`${FRONTEND}${REVIEW_PAGE}`);
        await expect(page.getByRole('button', { name: 'Add Review' })).toBeDisabled();
        await expect(page.getByText(REQUIREMENT)).toBeVisible();

        eligible = true;
        await page.reload();

        await expect(page.getByRole('button', { name: 'Add Review' })).toBeEnabled();
        await expect(page.getByText(REQUIREMENT)).toHaveCount(0);
        expect(requests).toBeGreaterThanOrEqual(2);
    });

    test('eligibility service failure keeps Add Review disabled and shows error', async ({ page }) => {
        await mockAuthenticatedCustomer(page);
        await page.route(`${API}/visits/reviews/eligibility`, async route => {
            await route.fulfill({
                status: 503,
                contentType: 'application/json',
                body: JSON.stringify({ message: 'Service unavailable' }),
            });
        });

        await page.goto(`${FRONTEND}${REVIEW_PAGE}`);

        await expect(page.getByRole('button', { name: 'Add Review' })).toBeDisabled();
        await expect(
            page.getByText('Unable to verify review eligibility. Please reload the page and try again.')
        ).toBeVisible();
        await expect(page.getByText(REQUIREMENT)).toHaveCount(0);
    });
});
