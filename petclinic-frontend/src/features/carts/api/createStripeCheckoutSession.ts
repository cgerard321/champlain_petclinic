import { useUser } from '@/context/UserContext';
import axiosInstance from '@/shared/api/axiosInstance';
import {
  StripeSessionRequestModel,
  StripeSessionResponseModel,
} from '../models/StripeSessionModel';
import { fetchCartIdByCustomerId } from './getCart';

type UseCreateStripeCheckoutSessionType = {
  createStripeSession: () => Promise<boolean>;
};

export function useCreateStripeCheckoutSession(): UseCreateStripeCheckoutSessionType {
  const { user } = useUser();

  const createStripeSession = async (): Promise<boolean> => {
    if (!user?.userId) {
      console.error('User is not authenticated');
      return false;
    }

    try {
      const cartId = await fetchCartIdByCustomerId(user.userId);
      if (!cartId) {
        console.error('Cart not found');
        return false;
      }

      const request: StripeSessionRequestModel = {
        cartId: cartId,
        frontendType: 'react',
        // so Stripe sends the customer back to this same site after paying
        frontendOrigin: window.location.origin,
      };

      const { data } = await axiosInstance.post<StripeSessionResponseModel>(
        `/carts/${encodeURIComponent(cartId)}/checkout/sessions`,
        request,
        { useV2: false }
      );

      if (!data.sessionUrl) {
        console.error('Stripe Checkout URL was not returned');
        return false;
      }

      window.location.assign(data.sessionUrl); //redirects browser to Stripe hosted payment page
      return true;
    } catch (error) {
      console.error('Error while creating Stripe Checkout session', error);
      return false;
    }
  };
  return { createStripeSession };
}
