import type { ShippingMethod } from '@/shared/api/cart';

export interface OrderItemModel {
  productId: string;
  imageId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number | null;
  quantityInCart: number;
  productQuantity: number;
}

export interface OrderModel {
  orderId: string;
  customerId: string;
  customerName: string;
  address: string;
  city: string;
  province?: string;
  postalCode: string;
  customerEmail: string;
  orderDate: string;
  products: OrderItemModel[];
  subtotal: number;
  promoPercent: number | null;
  tvq: number;
  tvc: number;
  total: number;
  shippingMethod?: ShippingMethod | null;
  shippingCost?: number;
  invoiceId: string | null;
}
