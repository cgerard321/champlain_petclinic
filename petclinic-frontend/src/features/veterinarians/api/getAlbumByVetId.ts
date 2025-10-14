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
  const res = await axiosInstance.get<AlbumPhotoDTO[]>(
    `/vets/${vetId}/albums`,
    {
      useV2: true,
      headers: { Accept: 'application/json' },
    }
  );
  return res.data ?? [];
};
