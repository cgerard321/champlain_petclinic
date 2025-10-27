import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const archiveVisit = async (
    visitId: string,
    visitRequest: VisitRequestModel
): Promise<VisitResponseModel> => {
    const response = await axiosInstance.put<VisitResponseModel>(
        `/visits/${visitId}/archive`,
        visitRequest,
        { useV2: false }
    );
    return response.data;
};
