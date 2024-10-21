import { NotificationModel } from '../models/ProductModels/NotificationModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getNotification } from './getNotification';

export async function updateNotification(
  productId: string,
  email: string,
  selectedTypes: string[]
): Promise<NotificationModel> {
  const existingNotification = await getNotification(productId);
  selectedTypes = selectedTypes.filter(type => type.trim() !== '');
  if (existingNotification.productId == '-1') {
    const res = await axiosInstance.post<NotificationModel>(
      `/notifications/${productId}`,
      { email, notificationType: selectedTypes },
      {
        responseType: 'json',
      }
    );
    return res.data;
  } else {
    const res = await axiosInstance.put<NotificationModel>(
      `/notifications/${existingNotification.productId}`,
      { email, notificationType: selectedTypes },
      {
        responseType: 'json',
      }
    );
    return res.data;
  }
}
