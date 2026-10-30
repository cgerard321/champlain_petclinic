export interface ReviewRequestDTO {
  rating: number;
  ownerId: string;
  reviewerId?: string;
  reviewerName: string;
  review: string;
  dateSubmitted: Date;
}
