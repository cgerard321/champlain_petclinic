import { test, expect } from '@playwright/test';
import { averageRating } from '../../src/features/products/utils/averageRatings';

test('includes ratings without written comments', () => {
  expect(
    averageRating([
      { rating: 5, review: '' },
      { rating: 2, review: 'Okay' },
    ])
  ).toEqual({ average: 3.5, count: 2 });
});

test('returns an empty state instead of zero when there are no scores', () => {
  expect(averageRating([])).toEqual({ average: null, count: 0 });
});

test('ignores non-finite and out-of-range scores', () => {
  const reviews = [NaN, Infinity, -1, 0, 6, 1, 5].map(rating => ({
    rating,
    review: '',
  }));
  expect(averageRating(reviews)).toEqual({ average: 3, count: 2 });
});

test('recalculates after addition, edit and deletion without mutating reviews', () => {
  const first = { rating: 2, review: '' };
  const added = { rating: 4, review: '' };
  expect(averageRating([first, added]).average).toBe(3);
  const edited = { ...added, rating: 5 };
  expect(averageRating([first, edited]).average).toBe(3.5);
  expect(averageRating([edited]).average).toBe(5);
  expect(averageRating([])).toEqual({ average: null, count: 0 });
  expect(first.rating).toBe(2);
  expect(added.rating).toBe(4);
});

test('category averages use only their own supplied scores and refresh after changes', () => {
  const legacy = { rating: 5, review: 'Old review' };
  const first = { rating: 4, review: '', qualityRating: 2, valueRating: 5 };
  const second = {
    rating: 1,
    review: '',
    qualityRating: 5,
    ingredientsRating: 3,
  };
  expect(averageRating([legacy], 'qualityRating')).toEqual({
    average: null,
    count: 0,
  });
  expect(averageRating([legacy, first, second], 'qualityRating')).toEqual({
    average: 3.5,
    count: 2,
  });
  expect(averageRating([first, second], 'valueRating')).toEqual({
    average: 5,
    count: 1,
  });
  expect(averageRating([first, second], 'ingredientsRating')).toEqual({
    average: 3,
    count: 1,
  });
  expect(averageRating([first, second], 'petSatisfactionRating')).toEqual({
    average: null,
    count: 0,
  });
  expect(
    averageRating([first, { ...second, qualityRating: 4 }], 'qualityRating')
      .average
  ).toBe(3);
  expect(averageRating([first], 'qualityRating').average).toBe(2);
  expect(
    averageRating(
      [
        { ...first, qualityRating: 0 },
        { ...second, qualityRating: null },
      ],
      'qualityRating'
    ).average
  ).toBeNull();
});
