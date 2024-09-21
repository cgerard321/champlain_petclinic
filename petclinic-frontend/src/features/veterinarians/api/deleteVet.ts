import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const deleteVet = async (vetId: string): Promise<AxiosResponse<void>> => {
    return await axiosInstance.delete<void>(`/vets/${vetId}`);
};