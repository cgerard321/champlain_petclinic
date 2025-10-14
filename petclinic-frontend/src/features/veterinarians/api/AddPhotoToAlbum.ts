import axiosInstance from '@/shared/api/axiosInstance';

export const addPhotoToAlbum = async (
  vetId: string,
  photoName: string,
  file: File
): Promise<void> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('photoName', photoName ?? file.name);

  await axiosInstance.post(`/vets/${vetId}/albums/photos`, formData, {
    useV2: true, // routes to /api/v2/gateway
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
