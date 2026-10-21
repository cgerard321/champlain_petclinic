export type RatingCategory =
  | 'quality'
  | 'value'
  | 'petSatisfaction'
  | 'ingredients';

export interface RatingModel {
  rating: number;
  categoryRatings?: Partial<Record<RatingCategory, number | null>>;
  review: string;
  customerId?: string;
  reviewerUsername?: string;
  reviewerPhoto?: string | null;
  likeCount?: number;
  likedByCurrentUser?: boolean;
}
