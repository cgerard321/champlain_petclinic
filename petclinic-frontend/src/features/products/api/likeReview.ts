import axiosInstance from '@/shared/api/axiosInstance.ts';

export interface ReviewLikeResult {
  likeCount: number;
  likedByCurrentUser: boolean;
}

export async function likeReview(
  productId: string,
  reviewCustomerId: string
): Promise<ReviewLikeResult> {
  const response = await axiosInstance.post<ReviewLikeResult>(
    `/ratings/${productId}/${reviewCustomerId}/likes`,
    undefined,
    { useV2: false, handleLocally: true }
  );

  return response.data;
}
