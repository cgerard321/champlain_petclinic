import axiosInstance from '@/shared/api/axiosInstance';

export interface AlbumPhotoDTO {
  id: number;
  vetId: string;
  filename: string;
  imgType: string;
  data: string;
}

export const getAlbumsByVetId = async (
  vetId: string
): Promise<AlbumPhotoDTO[]> => {
  try {
    const response = await axiosInstance.get<string>(`/vets/${vetId}/albums`, {
      responseType: 'text',
      useV2: true,
    });
    return response.data
      .split('data:')
      .map((payLoad: string) => {
        try {
          if (payLoad === '') return null;
          return JSON.parse(payLoad);
        } catch (err) {
          return null;
        }
      })
      .filter((data?: JSON) => data !== null);
  } catch (error) {
    return [];
  }
};
