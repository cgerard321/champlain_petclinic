export interface StripeSessionRequestModel {
  cartId: string;
  frontendTypeUrl: 'react';
}

export interface StripeSessionResponseModel {
  sessionId: string;
  sessionUrl: string;
}
