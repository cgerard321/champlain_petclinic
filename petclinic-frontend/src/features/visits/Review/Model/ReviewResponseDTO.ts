export interface ReviewResponseDTO {
  reviewId: number;
  ownerId: string;
  rating: number;
  reviewerId?: string;
  reviewerName: string;
  review: string;
  dateSubmitted: Date;
}
