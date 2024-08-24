import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function deleteAllInventories(): Promise<void> {
  axiosInstance.delete(axiosInstance.defaults.baseURL + 'inventories');
}
