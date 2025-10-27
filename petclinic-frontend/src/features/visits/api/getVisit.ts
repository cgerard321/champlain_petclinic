import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const getVisit = async (
    visitId: string,
    includePrescription: boolean = false
): Promise<VisitResponseModel> => {
    const response = await axiosInstance.get<VisitResponseModel>(
        `/visits/${visitId}`,
        {
            params: { includePrescription },
            useV2: false
        }
    );
    return response.data;
};
