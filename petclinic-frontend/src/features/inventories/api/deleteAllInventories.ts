import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function deleteAllInventories(): Promise<void> {
  try {
    axiosInstance.delete('/inventory', { useV2: false });
  } catch (error) {
    console.error('Error deleting Inventories:', error);
    throw error;
  }
}
