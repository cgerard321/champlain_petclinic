// src/shared/api/cart.ts
import axiosInstance from '@/shared/api/axiosInstance';

export interface CartProductModel {
  productId: string;
  productName: string;
  productSalePrice: number;
  quantityInCart: number;
}
export interface CartDetailsModel {
  cartId: string;
  customerId: string;
  customerName?: string;
  products: CartProductModel[];
  wishListProducts?: CartProductModel[];
  subtotal: number;
  tvq: number;
  tvc: number;
  total: number;
  promoPercent?: number | null;
  message?: string | null;
}

export async function applyPromo(
  cartId: string,
  promoPercent: number
): Promise<CartDetailsModel | null> {
  const res = await axiosInstance.put(`/carts/${cartId}/promo`, null, {
    params: { promoPercent },
    handleLocally: true,
    useV2: false,
  });
  return res.data as CartDetailsModel | null;
}

export async function clearPromo(
  cartId: string
): Promise<{ promoPercent?: number | null }> {
  const { data } = await axiosInstance.put(
    `/carts/${cartId}/promo/clear`,
    {},
    { useV2: false }
  );
  return data as { promoPercent?: number | null };
}
