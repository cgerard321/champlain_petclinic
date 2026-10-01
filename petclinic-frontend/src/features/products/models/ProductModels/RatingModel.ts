export interface RatingModel {
  rating: number;
  review: string;
  customerId?: string;
  reviewerUsername?: string;
  reviewerPhoto?: string | null;
}
