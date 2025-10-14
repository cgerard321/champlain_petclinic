import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axios from 'axios';

export async function searchInventories(
  currentPage: number,
  listSize: number,
  inventoryName?: string,
  inventoryType?: string,
  inventoryDescription?: string,
  importantOnly?: boolean
): Promise<Inventory[]> {
  try {
    const queryParams = new URLSearchParams();
    if (inventoryName) queryParams.append('inventoryName', inventoryName);
    if (inventoryType) queryParams.append('inventoryType', inventoryType);
    if (inventoryDescription)
      queryParams.append('inventoryDescription', inventoryDescription);
    if (importantOnly) queryParams.append('importantOnly', 'true');

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventories?page=${currentPage}&size=${listSize}&${queryString}`
      : `/inventories?page=${currentPage}&size=${listSize}`;

    const response = await axiosInstance.get<Inventory[]>(url, {
      useV2: false,
    });
    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    const status = error.response?.status ?? 0;
    const payload: unknown = error.response?.data;

    const data =
      payload && typeof payload === 'object'
        ? (payload as Record<string, unknown>)
        : undefined;

    const serverMessage = typeof data?.message === 'string' ? data.message : '';

    switch (status) {
      case 400: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Invalid inventory data. Please review your input and try again.'
        );
      }
      case 404: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Inventory resource was not found.'
        );
      }
      case 429: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Too many requests. Please try again later.'
        );
      }
      default:
        throw error;
    }
  }
}
