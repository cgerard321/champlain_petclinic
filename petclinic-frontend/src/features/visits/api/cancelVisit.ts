// import axiosInstance from '@/shared/api/axiosInstance.ts';
// import { Visit } from '@/features/visits/models/Visit.ts';
// import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
//
// export async function cancelVisit(
//   visitId: string,
//   onSuccess: (updatedVisit: Visit) => void
// ): Promise<void> {
//   try {
//     const patchResponse = await axiosInstance.patch<VisitResponseModel>(
//       `/visits/${visitId}/status/CANCELLED`,
//       {},
//       { useV2: false }
//     );
//
//     const updatedVisit = patchResponse.data;
//     onSuccess(updatedVisit);
//   } catch (error) {
//     console.error('Error canceling visit:', error);
//     throw error;
//   }
// }
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const cancelVisit = async (
    visitId: string
): Promise<VisitResponseModel> => {
    const response = await axiosInstance.patch<VisitResponseModel>(
        `/visits/${visitId}`,
        null,
        {
            params: { status: 'CANCELLED' },
            useV2: false
        }
    );
    return response.data;
};
