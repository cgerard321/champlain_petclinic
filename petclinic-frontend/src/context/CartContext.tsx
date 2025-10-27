// src/context/CartContext.tsx
import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  ReactNode,
  useCallback,
} from 'react';
import { useUser } from '@/context/UserContext';
import { Role } from '@/shared/models/Role';
import {
  getCartIdFromLS,
  setCartIdInLS,
  getCartCountFromLS,
  setCartCountInLS,
  notifyCartChanged,
  CART_CHANGED,
} from '@/features/carts/api/cartEvent';
import {
  fetchCartIdByCustomerId,
  fetchCartDetailsByCartId,
} from '@/features/carts/api/getCart';

interface CartContextType {
  cartId: string | null;
  cartCount: number;
  setCartId: (id: string | null) => void;
  setCartCount: (count: number) => void;
  refreshFromAPI: () => Promise<{ cartId: string | null; cartCount: number }>;
  syncAfterAddToCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({
  children,
}: {
  children: ReactNode;
}): JSX.Element {
  const { user } = useUser();
  const [cartId, setCartIdState] = useState<string | null>(getCartIdFromLS());
  const [cartCount, setCartCountState] = useState<number>(getCartCountFromLS());

  const roleList = useMemo<Role[]>(() => {
    const rawRoles = user?.roles;
    if (!rawRoles) return [];
    if (rawRoles instanceof Set) {
      return Array.from(rawRoles as Set<Role>);
    }
    if (Array.isArray(rawRoles)) {
      return rawRoles as Role[];
    }
    return [];
  }, [user?.roles]);

  const isOwner = useMemo(
    () => roleList.some(role => role?.name === 'OWNER'),
    [roleList]
  );

  const setCartId = (id: string | null): void => {
    setCartIdState(id);
    if (id) {
      setCartIdInLS(id);
    } else {
      localStorage.removeItem('cart:id');
    }
  };

  const setCartCount = (n: number): void => {
    const safe = Math.max(0, Math.trunc(n));
    setCartCountState(safe);
    setCartCountInLS(safe);
  };

  // Loads the cart ID and item count from the backend if needed
  const refreshFromAPI = useCallback(async (): Promise<{
    cartId: string | null;
    cartCount: number;
  }> => {
    if (!isOwner || !user?.userId) {
      setCartId(null);
      setCartCount(0);
      return { cartId: null, cartCount: 0 };
    }

    // Try localStorage first to avoid unnecessary requests
    let id = getCartIdFromLS() || cartId;
    if (!id) {
      id = await fetchCartIdByCustomerId(user.userId);
      if (id) {
        setCartId(id);
        notifyCartChanged();
      } else {
        // user has no active cart yet
        setCartCount(0);
        return { cartId: null, cartCount: 0 };
      }
    }

    const cart = await fetchCartDetailsByCartId(id);
    const count = Array.isArray(cart?.products)
      ? cart.products.reduce((acc, product) => {
          const quantity = Number(product?.quantityInCart);
          if (Number.isFinite(quantity) && quantity > 0) {
            return acc + Math.trunc(quantity);
          }
          return acc + 1;
        }, 0)
      : 0;
    const fallbackCount =
      count > 0
        ? count
        : Array.isArray(cart?.products)
          ? cart.products.length
          : 0;

    setCartCount(fallbackCount);
    return { cartId: id, cartCount: fallbackCount };
  }, [user?.userId, cartId, isOwner]);

  // Force sync after "Add to Cart" to prevent UI mismatch
  const syncAfterAddToCart = useCallback(async () => {
    try {
      const id = cartId || (await fetchCartIdByCustomerId(user?.userId));
      if (!id) return;
      setCartId(id);
      const count = await fetchCartCountByCartId(id);
      setCartCount(count);
    } catch (err) {
      console.error('Failed to sync cart after add:', err);
    }
  }, [user?.userId, cartId]);

  // When the user logs in, check if we already have cart data in localStorage.
  // If not, fetch it from the API to keep the cart state in sync.
  useEffect(() => {
    if (!isOwner) {
      setCartId(null);
      setCartCount(0);
      return;
    }
    if (!user?.userId) return;
    let alive = true;
    (async () => {
      const cached = getCartCountFromLS();
      if (cached > 0) {
        if (!alive) return;
        setCartCount(cached);
        return;
      }
      try {
        if (!alive) return;
        await refreshFromAPI();
      } catch {
        // Ignore temporary network issues, data will sync later
      }
    })();
    return () => {
      alive = false;
    };
  }, [user?.userId, refreshFromAPI, isOwner]);

  // Sync with updates from other components/tabs (Products page, etc.)
  useEffect(() => {
    if (!isOwner) {
      setCartCountState(0);
      setCartIdState(null);
      return;
    }
    const syncFromLocalStorage = (): void => {
      const n = getCartCountFromLS();
      setCartCountState(Math.max(0, Math.trunc(n)));
    };
    const onStorage = (e: StorageEvent): void => {
      if (e.key === 'cart:changed' || e.key === 'cart:count') {
        syncFromLocalStorage();
      }
    };
    //same-tab
    window.addEventListener(
      CART_CHANGED as unknown as string,
      syncFromLocalStorage as EventListener
    );
    //cross-tab
    window.addEventListener('storage', onStorage);
    return () => {
      window.removeEventListener(
        CART_CHANGED as unknown as string,
        syncFromLocalStorage as EventListener
      );
      window.removeEventListener('storage', onStorage);
    };
  }, [isOwner]);

  const value = useMemo<CartContextType>(
    () => ({
      cartId,
      cartCount,
      setCartId,
      setCartCount,
      refreshFromAPI,
      syncAfterAddToCart,
    }),
    [cartId, cartCount, refreshFromAPI, syncAfterAddToCart]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextType {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used inside a CartProvider');
  return ctx;
}
