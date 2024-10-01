import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function addImage(formData: FormData): Promise<void> {
  try {
    const response = await axiosInstance.post('/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      responseType: 'json',
    });
    return response.data;
  } catch (error) {
    console.error('Error adding image:', error);
    throw error;
  }
}
