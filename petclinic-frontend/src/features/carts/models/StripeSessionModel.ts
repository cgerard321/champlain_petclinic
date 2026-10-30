export interface StripeSessionRequestModel {
  cartId: string;
  frontendType: 'react';
  frontendOrigin: string;
}

export interface StripeSessionResponseModel {
  sessionId: string;
  sessionUrl: string;
}
