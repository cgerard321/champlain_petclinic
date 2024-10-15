import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const deleteVetEducation = async (
    vetId: string,
    educationId: string
): Promise<AxiosResponse<void>> => {
    return await axiosInstance.delete<void>(
        `/vets/${vetId}/educations/${educationId}`
    );
};