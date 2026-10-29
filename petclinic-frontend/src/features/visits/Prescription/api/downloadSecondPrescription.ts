import axiosInstance from '@/shared/api/axiosInstance';

export const downloadSecondPrescription = async (
    visitId: string
): Promise<Blob> => {
    const response = await axiosInstance.get(
        `/visits/${visitId}/prescription/2/pdf`,
        {
            responseType: 'arraybuffer',
            useV2: false,
        }
    );

    return new Blob([response.data], { type: 'application/pdf' });
};