import { NotificationModel } from '@/features/products/models/ProductModels/NotificationModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function getNotification(
  productId: string
): Promise<NotificationModel> {
  try {
    const res = await axiosInstance.get<NotificationModel>(
      '/notifications/' + productId,
      {
        responseType: 'json',
      }
    );
    return res.data;
  } catch (error) {
    return {
      productId: '-1',
      productName: '-1',
      notificationType: [''],
      email: '',
    };
  }
}
