export interface PromoCodeRequestModel {
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
}