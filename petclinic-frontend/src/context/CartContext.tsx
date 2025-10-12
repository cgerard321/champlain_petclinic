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
  fetchCartCountByCartId,
} from '@/features/carts/api/getCart';

interface CartContextType {
  cartId: string | null;
  cartCount: number;
  setCartId: (id: string | null) => void;
  setCartCount: (count: number) => void;
  refreshFromAPI: () => Promise<{ cartId: string | null; cartCount: number }>;
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

  const setCartId = (id: string | null): void => {
    setCartIdState(id);
    if (id) setCartIdInLS(id);
  };

  const setCartCount = (n: number): void => {
    const safe = Math.max(0, Math.trunc(n));
    setCartCountState(safe);
    setCartCountInLS(safe);
    notifyCartChanged();
  };

  // Loads the cart ID and item count from the backend if needed
  const refreshFromAPI = useCallback(async (): Promise<{
    cartId: string | null;
    cartCount: number;
  }> => {
    if (!user?.userId) {
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

    const count = await fetchCartCountByCartId(id);
    setCartCount(count);
    return { cartId: id, cartCount: count };
  }, [user?.userId, cartId]);

  // When the user logs in, check if we already have cart data in localStorage.
  // If not, fetch it from the API to keep the cart state in sync.
  useEffect(() => {
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
  }, [user?.userId, refreshFromAPI]);

  // Sync with updates from other components/tabs (Products page, etc.)
  useEffect(() => {
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
  }, []);

  const value = useMemo<CartContextType>(
    () => ({
      cartId,
      cartCount,
      setCartId,
      setCartCount,
      refreshFromAPI,
    }),
    [cartId, cartCount, refreshFromAPI]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextType {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used inside a CartProvider');
  return ctx;
}
