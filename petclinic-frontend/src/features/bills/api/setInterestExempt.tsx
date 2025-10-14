import axiosInstance from '@/shared/api/axiosInstance';

export const setInterestExempt = async (
  billId: string,
  exempt: boolean
): Promise<void> => {
  try {
    await axiosInstance.patch(
      `/bills/${billId}/exempt-interest?exempt=${exempt}`
    );
  } catch (error) {
    console.error('Failed to set interest exemption:', error);
    throw new Error('Failed to update interest exemption');
  }
};
