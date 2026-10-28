import { expect, test } from '@playwright/test';
import {
  cleanReviewText,
  isReviewProfane,
} from '../../src/features/visits/Review/ReviewProfanity';

test('detects package terms and the curated Quebec French additions', () => {
  expect(isReviewProfane('This visit was shit.')).toBe(true);
  expect(isReviewProfane('The service was tabarnak.')).toBe(true);
});

test('normalizes accents, leetspeak, and punctuation between letters', () => {
  expect(isReviewProfane('The service was càlice.')).toBe(true);
  expect(isReviewProfane('The service was t@b@rn@k.')).toBe(true);
  expect(isReviewProfane('The service was t.a.b.a.r.n.a.k.')).toBe(true);
});

test('does not flag harmless words that contain short profanity fragments', () => {
  expect(
    isReviewProfane(
      'The passenger sat in class and enjoyed the classic design.'
    )
  ).toBe(false);
});

test('masks only the detected word and preserves the rest of the review', () => {
  expect(cleanReviewText('Helpful staff; tabarnak service!')).toBe(
    'Helpful staff; ******** service!'
  );
});

test('masks normalized evasions while retaining punctuation outside the word', () => {
  expect(cleanReviewText('Friendly care; t.a.b.a.r.n.a.k!')).toBe(
    'Friendly care; ***************!'
  );
});
