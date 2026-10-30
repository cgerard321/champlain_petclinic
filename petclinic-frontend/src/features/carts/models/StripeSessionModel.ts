export interface StripeSessionRequestModel {
  cartId: string;
  frontendType: 'react';
}

export interface StripeSessionResponseModel {
  sessionId: string;
  sessionUrl: string;
}
