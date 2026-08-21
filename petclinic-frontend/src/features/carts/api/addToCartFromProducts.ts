import axiosInstance from '@/shared/api/axiosInstance';
import { AxiosError } from 'axios';
import { fetchCartIdByCustomerId } from './getCart';
import { useUser } from '@/context/UserContext';
import { bumpCartCountInLS, getCartIdFromLS, setCartIdInLS } from './cartEvent';
import type { Role } from '@/shared/models/Role';

type UseAddToCartReturnType = {
  addToCart: (productId: string, quantity?: number) => Promise<boolean>;
};

type CreateCartResponse = {
  cartId?: string;
  id?: string;
  [k: string]: unknown;
};

export function useAddToCart(): UseAddToCartReturnType {
  const { user } = useUser();

  const getOrCreateCartId = async (userId: string): Promise<string> => {
    const cachedId = getCartIdFromLS();
    if (cachedId) {
      return cachedId;
    }

    const existingCartId = await fetchCartIdByCustomerId(userId);
    if (existingCartId) {
      setCartIdInLS(existingCartId);
      return existingCartId;
    }

    try {
      const { data } = await axiosInstance.post<CreateCartResponse>(
        '/carts', // → POST /api/gateway/carts
        { customerId: userId },
        {
          headers: { 'Content-Type': 'application/json' },
          useV2: false,
        }
      );
      const newId = (data?.cartId ?? data?.id) as string | undefined;
      if (!newId) throw new Error('Could not create cart');
      setCartIdInLS(newId);
      return newId;
    } catch (err) {
      const ax = err as AxiosError;
      const status = ax.response?.status;

      if (status === 401) {
        console.error('User is not authorized to create a cart');
      }

      throw err;
    }
  };

  const addToCart = async (
    productId: string,
    requestedQuantity?: number
  ): Promise<boolean> => {
    // pas connecté → on ne tente rien
    if (!user?.userId) return false;

    const normalizedProductId = productId?.trim();
    if (!normalizedProductId) return false;

    const coercedQuantity =
      typeof requestedQuantity === 'number'
        ? requestedQuantity
        : Number.parseInt(String(requestedQuantity ?? ''), 10);

    const quantity =
      Number.isFinite(coercedQuantity) && coercedQuantity > 0
        ? Math.floor(coercedQuantity)
        : 1;

    // ---- Rôles utilisateur (simple et lisible) ----
    const roleNames = new Set<string>();
    const rolesSet = user?.roles as Set<Role> | undefined; // Set<{ id:number; name:string }>

    if (rolesSet) {
      for (const role of rolesSet) {
        roleNames.add(role.name);
      }
    }

    // staff/admin → on ne tente rien
    const isStaff =
      roleNames.has('ADMIN') ||
      roleNames.has('EMPLOYEE') ||
      roleNames.has('VET') ||
      roleNames.has('INVENTORY_MANAGER') ||
      roleNames.has('RECEPTIONIST');

    if (isStaff) return false;

    try {
      const cartId = await getOrCreateCartId(user.userId);

      await axiosInstance.post(
        `/carts/${encodeURIComponent(cartId)}/products`,
        {
          productId: normalizedProductId,
          quantity,
        },
        { useV2: false }
      );

      //keep navbar offline, persist id + bump count locally
      setCartIdInLS(cartId);
      bumpCartCountInLS(quantity);

      return true;
    } catch (err) {
      const ax = err as AxiosError;
      const status = ax.response?.status ?? 'unknown';
      const payload = ax.response?.data ?? ax.message;
      console.error('AddToCart failed:', status, payload);
      // pas d'alert : l'UI affichera un message propre si nécessaire
      return false;
    }
  };

  return { addToCart };
}
