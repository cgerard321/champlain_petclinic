import axiosInstance from '@/shared/api/axiosInstance';
import type { OrderModel } from '../models/OrderModel';

export const getCustomerOrders = async (
  customerId: string
): Promise<OrderModel[]> => {
  const { data } = await axiosInstance.get<OrderModel[]>(
    `/orders/customer/${encodeURIComponent(customerId)}`,
    { useV2: true }
  );

  return data;
};
