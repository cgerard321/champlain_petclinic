import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const getAllVetVisits = async (
    practitionerId: string
): Promise<VisitResponseModel[]> => {
    const response = await axiosInstance.get(
        `/visits`,
        {
            params: { practitionerId },
            responseType: 'text',
            useV2: false
        }
    );

    const text = response.data;
    if (!text) return [];

    // Parse SSE format: extract data: lines
    const visits: VisitResponseModel[] = [];
    const lines = text.split('\n');

    for (const line of lines) {
        if (line.startsWith('data:')) {
            try {
                const json = line.substring(5).trim();
                if (json) {
                    visits.push(JSON.parse(json));
                }
            } catch (e) {
                // skip invalid lines
            }
        }
    }

    return visits;
};
