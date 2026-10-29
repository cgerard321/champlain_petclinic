import axiosInstance from '@/shared/api/axiosInstance';
import { AddressSuggestionModel } from '../models/AddressSuggestionModel';

export const getAddressSuggestions = async (
  query: string
): Promise<AddressSuggestionModel[]> => {
  try {
    const response = await axiosInstance.get<AddressSuggestionModel[]>(
      '/addresses/autocomplete',
      {
        params: { query },
        useV2: false,
        handleLocally: true,
      }
    );
    return Array.isArray(response.data) ? response.data : [];
  } catch {
    return [];
  }
};
