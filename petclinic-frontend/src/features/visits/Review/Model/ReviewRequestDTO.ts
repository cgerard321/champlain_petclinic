export interface ReviewRequestDTO {
  rating: number;
  ownerId: string;
  reviewerName: string;
  review: string;
  dateSubmitted: Date;
}
