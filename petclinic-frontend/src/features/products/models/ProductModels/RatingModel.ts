export interface RatingCategories {
  qualityRating: number;
  valueRating: number;
  petSatisfactionRating: number;
  ingredientsRating: number;
}

export interface RatingModel {
  rating: number;
  review: string;
  qualityRating?: number;
  valueRating?: number;
  petSatisfactionRating?: number;
  ingredientsRating?: number;
  customerId?: string;
  reviewerUsername?: string;
  reviewerPhoto?: string | null;
  likeCount?: number;
  likedByCurrentUser?: boolean;
}
