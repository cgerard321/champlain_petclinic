export interface Promo {
  id: string;
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
  active: boolean;
}

export interface PromoRequest {
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
  active: boolean;
}
