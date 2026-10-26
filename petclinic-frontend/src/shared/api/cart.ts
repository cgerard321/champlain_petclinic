// src/shared/api/cart.ts
import axiosInstance from '@/shared/api/axiosInstance';

export type ShippingMethod = 'PICKUP' | 'STANDARD' | 'EXPRESS';

export interface CartProductModel {
  promoLookupFailed?: boolean;
  productId: string;
  productName: string;
  productSalePrice: number;
  quantityInCart: number;
  discountAmount?: number;
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
  discountAmount?: number;
  shippingMethod?: ShippingMethod | null;
  shippingCost?: number;
  message?: string | null;
}

export async function applyPromo(
  cartId: string,
  promoCode: string
): Promise<CartDetailsModel | null> {
  const res = await axiosInstance.put(
    `/carts/${cartId}/promo`,
    { promoCode },
    {
      handleLocally: true,
      useV2: false,
    }
  );
  return res.data as CartDetailsModel | null;
}

export async function clearPromo(cartId: string): Promise<void> {
  await axiosInstance.delete(`/carts/${cartId}/promo`, { useV2: false });
}

export async function updateShippingMethod(
  cartId: string,
  shippingMethod: ShippingMethod
): Promise<CartDetailsModel | null> {
  const res = await axiosInstance.put(
    `/carts/${cartId}/shipping`,
    { shippingMethod },
    {
      handleLocally: true,
      useV2: false,
    }
  );
  return res.data as CartDetailsModel | null;
}
