import { Inventory } from '@/features/inventories/models/Inventory';
import axiosInstance from '@/shared/api/axiosInstance';
import axios from 'axios';

export default async function addInventory(
  inventoryData: Omit<Inventory, 'inventoryId'> // Renamed to avoid confusion
): Promise<void> {
  try {
    // Append the appropriate endpoint for adding an inventory
    await axiosInstance.post<void>('/inventories', inventoryData, {
      useV2: false,
    });
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    const status = error.response?.status ?? 0;

    switch (status) {
      case 400:
        const msg = 'Invalid inventory data. Please review your inputs.';
        throw new Error(msg);
      case 404:
        throw new Error('Inventory resource was not found.');
      case 409:
        throw new Error(
          'An inventory with the similar identifier already exists.'
        );
      case 422:
        throw new Error('Validation failed. Please check your input fields.');
      case 429:
        throw new Error('Too many requests. Please try again later.');
      default:
        throw error;
    }
  }
}
