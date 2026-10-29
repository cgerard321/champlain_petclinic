
import { expect, test, type Page } from '@playwright/test';

interface Review {
    reviewId: number;
    ownerId: string;
    rating: number;
    reviewerName: string;
    review: string;
    dateSubmitted: string;
}

const reviews: Review[] = [1, 2, 3, 4, 5, 3].map((rating, index) => ({
    reviewId: index + 1,
    ownerId: `owner-${index + 1}`,
    rating,
    reviewerName: `Reviewer ${index + 1}`,
    review: `Review ${index + 1} with rating ${rating}`,
    dateSubmitted: '2026-10-20T12:00:00Z',
}));

async function mockReviewApi(page: Page, data: Review[]): Promise<void> {
    // Intercept API requests before the page loads.
    await page.route('**/api/**', route =>
        route.fulfill({ status: 200, json: [] })
    );

    await page.route('**/api/gateway/users/jwt', route =>
        route.fulfill({
            status: 200,
            json: {
                username: 'review-test-customer',
                userId: 'customer-1',
                email: 'review-test@example.com',
                roles: ['CUST'],
            },
        })
    );

    await page.route('**/api/gateway/visits/reviews', route =>
        route.fulfill({ status: 200, json: data })
    );

    await page.route(
        /\/api\/gateway\/customers\/customer-1(?:\?.*)?$/,
        route =>
            route.fulfill({
                status: 200,
                json: {
                    customerId: 'customer-1',
                    firstName: 'Test',
                    lastName: 'Customer',
                },
            })
    );

    await page.route('**/api/gateway/customers/customer-1/cart', route =>
        route.fulfill({ status: 200, json: { cartId: null } })
    );
}

for (const path of ['/reviews', '/customers/visits/reviews']) {
    test.describe(`Rating filter on ${path}`, () => {
        test('shows all reviews by default, filters each rating, and resets', async ({
                                                                                         page,
                                                                                     }) => {
            await mockReviewApi(page, reviews);
            await page.goto(`http://localhost:3000${path}`);

            const filter = page.getByRole('combobox', {
                name: 'Filter by rating:',
            });

            const reviewCells = page.locator(
                '.reviews-table tbody tr td:nth-child(2)'
            );

            await expect(filter.locator('option')).toHaveText([
                'All ratings',
                '1 star',
                '2 stars',
                '3 stars',
                '4 stars',
                '5 stars',
            ]);

            await expect(filter).toHaveValue('all');

            await expect(reviewCells).toHaveText(
                reviews.map(item => item.review)
            );

            for (const rating of [1, 2, 3, 4, 5]) {
                await filter.selectOption(String(rating));

                await expect(reviewCells).toHaveText(
                    reviews
                        .filter(item => item.rating === rating)
                        .map(item => item.review)
                );
            }

            await filter.selectOption('all');

            await expect(reviewCells).toHaveText(
                reviews.map(item => item.review)
            );
        });

        test('shows a message when a rating has no matching reviews', async ({
                                                                                 page,
                                                                             }) => {
            const withoutFiveStarReviews = reviews.filter(
                item => item.rating !== 5
            );

            await mockReviewApi(page, withoutFiveStarReviews);
            await page.goto(`http://localhost:3000${path}`);

            const filter = page.getByRole('combobox', {
                name: 'Filter by rating:',
            });

            const reviewRows = page.locator('.reviews-table tbody tr');

            await expect(reviewRows).toHaveCount(
                withoutFiveStarReviews.length
            );

            await filter.selectOption('5');

            await expect(reviewRows).toHaveCount(1);

            await expect(reviewRows.first()).toContainText(
                'No reviews found for this rating'
            );

            await filter.selectOption('all');

            await expect(reviewRows).toHaveCount(
                withoutFiveStarReviews.length
            );
        });
    });
}
