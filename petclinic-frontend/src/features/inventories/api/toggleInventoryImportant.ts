import axiosInstance from '@/shared/api/axiosInstance';

export const toggleInventoryImportant = async (
  inventoryId: string,
  isImportant: boolean
): Promise<void> => {
  try {
    await axiosInstance.patch<void>(
      `/inventory/${inventoryId}/important`,
      { important: isImportant },
      { useV2: false }
    );
  } catch (error) {
    console.error('Error toggling inventory important status:', error);
    throw error;
  }
};
