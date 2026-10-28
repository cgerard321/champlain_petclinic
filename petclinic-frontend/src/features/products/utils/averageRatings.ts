import type {
  RatingModel,
  RatingCategories,
} from '../models/ProductModels/RatingModel';

export type RatingCategory = keyof RatingCategories;

type RatingsWithCategories = Omit<RatingModel, RatingCategory> &
  Partial<Record<RatingCategory, number | null>>;

export interface RatingAverage {
  average: number | null;
  count: number;
}

// Missing and invalid scores must not count as zero or dilute an average.
export function averageRating(
  reviews: readonly RatingsWithCategories[],
  category?: RatingCategory
): RatingAverage {
  const scores = reviews
    .map(review => (category ? review[category] : review.rating))
    .filter(
      (score): score is number =>
        typeof score === 'number' &&
        Number.isFinite(score) &&
        score >= 1 &&
        score <= 5
    );
  return {
    average: scores.length
      ? scores.reduce((sum, score) => sum + score, 0) / scores.length
      : null,
    count: scores.length,
  };
}
