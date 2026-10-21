import { test, expect } from '@playwright/test';
import { averageRating } from '../../src/features/products/utils/averageRatings';
import type { RatingModel } from '../../src/features/products/models/ProductModels/RatingModel';

test('legacy reviews contribute only to overall, including ratings without text', () => {
  const reviews = [
    { rating: 5, review: '' },
    { rating: 2, review: 'Okay' },
  ];
  expect(averageRating(reviews)).toEqual({ average: 3.5, count: 2 });
  expect(averageRating(reviews, 'quality')).toEqual({
    average: null,
    count: 0,
  });
});

test('each category uses only its own submitted scores', () => {
  const reviews: RatingModel[] = [
    { rating: 5, review: '', categoryRatings: { quality: 2, value: 5 } },
    { rating: 1, review: '', categoryRatings: { quality: 4, ingredients: 3 } },
    { rating: 3, review: '', categoryRatings: { quality: null } },
  ];
  expect(averageRating(reviews, 'quality')).toEqual({ average: 3, count: 2 });
  expect(averageRating(reviews, 'value')).toEqual({ average: 5, count: 1 });
  expect(averageRating(reviews, 'ingredients')).toEqual({
    average: 3,
    count: 1,
  });
  expect(averageRating(reviews, 'petSatisfaction')).toEqual({
    average: null,
    count: 0,
  });
});

test('ignores non-finite and out-of-range scores', () => {
  const reviews = [NaN, Infinity, -1, 0, 6, 1, 5].map(rating => ({
    rating,
    review: '',
    categoryRatings: { value: rating },
  }));
  expect(averageRating(reviews)).toEqual({ average: 3, count: 2 });
  expect(averageRating(reviews, 'value')).toEqual({ average: 3, count: 2 });
});

test('recalculates after addition, edit and deletion without mutating reviews', () => {
  const first = { rating: 2, review: '', categoryRatings: { quality: 1 } };
  const added = { rating: 4, review: '', categoryRatings: { quality: 5 } };
  expect(averageRating([first, added], 'quality').average).toBe(3);
  const edited = { ...added, categoryRatings: { quality: 3 } };
  expect(averageRating([first, edited], 'quality').average).toBe(2);
  expect(averageRating([edited], 'quality').average).toBe(3);
  expect(averageRating([], 'quality')).toEqual({ average: null, count: 0 });
  expect(first.categoryRatings.quality).toBe(1);
  expect(added.categoryRatings.quality).toBe(5);
});
