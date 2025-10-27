import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const addVisit = async (
    visitRequest: VisitRequestModel
): Promise<VisitResponseModel> => {
    const response = await axiosInstance.post<VisitResponseModel>(
        `/visits`,
        visitRequest,
        { useV2: false }
    );
    return response.data;
};
