import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel.ts';
import { AxiosResponse } from 'axios';

export const getPet = async (
    petId: string,
    ownerId?: string
):Promise<AxiosResponse<PetResponseModel>> => {
    const params = { useV2: false, includePhoto: true };

    if (ownerId) {
        return await axiosInstance.get<PetResponseModel>(
            `/pets/owners/${ownerId}/pets/${petId}`,
            { params }
        );
    } else {
        return await axiosInstance.get<PetResponseModel>(`/pets/${petId}`, {
            params,
        });
    }
};
