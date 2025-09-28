import axiosInstance from '@/shared/api/axiosInstance';

export interface AlbumUploadResponse {
  id?: number | string;
  imgType: string;
  data: string; // base64 from backend
  filename?: string;
  vetId?: string;
}

export const addAlbumPhoto = async (
  vetId: string,
  photoName: string,
  file: File
): Promise<AlbumUploadResponse> => {
  const formData = new FormData();
  // IMPORTANT: the keys must match @RequestPart names on the BFF:
  // @RequestPart("photoName") and @RequestPart("file")
  formData.append('photoName', photoName);
  formData.append('file', file);

  // Use the API GATEWAY (v2). If your axiosInstance supports `useV2`,
  // set it to true so baseURL becomes /api/v2/gateway.
  const { data } = await axiosInstance.post(`/vets/${vetId}/albums`, formData, {
    useV2: true,
    headers: { 'Content-Type': 'multipart/form-data' },
  });

  return data as AlbumUploadResponse;
};
