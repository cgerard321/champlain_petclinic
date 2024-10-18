export interface ReviewResponseDTO {
  reviewId: number;
  ownerId: string;
  rating: number;
  reviewerName: string;
  review: string;
  dateSubmitted: Date;
}
