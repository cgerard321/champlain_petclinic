// cartEvent.ts
export const CART_CHANGED = 'cart:changed';

const CART_COUNT_KEY = 'cart:count';
const CART_ID_KEY = 'cart:id';

export function notifyCartChanged(): void {
  // same-tab listeners
  window.dispatchEvent(new Event(CART_CHANGED));
  // cross-tab listeners
  try {
    localStorage.setItem('cart:changed', String(Date.now()));
  } catch {
    // private mode etc — ignore
  }
}

/** Read / write cartId in localStorage */
export function getCartIdFromLS(): string | null {
  try {
    return localStorage.getItem(CART_ID_KEY);
  } catch {
    return null;
  }
}

export function setCartIdInLS(id: string | null | undefined): void {
  try {
    if (id == null) {
      localStorage.removeItem(CART_ID_KEY);
    } else {
      localStorage.setItem(CART_ID_KEY, id);
    }
  } catch {
    /* ignore */
  }
}

/** Read / write cart count in localStorage (single source of truth for navbar) */
export function getCartCountFromLS(): number {
  try {
    const raw = localStorage.getItem(CART_COUNT_KEY);
    const n = raw ? Number(raw) : 0;
    return Number.isFinite(n) && n >= 0 ? n : 0;
  } catch {
    return 0;
  }
}

export function setCartCountInLS(n: number): void {
  try {
    localStorage.setItem(CART_COUNT_KEY, String(Math.max(0, Math.trunc(n))));
  } catch {
    /* ignore */
  }
  notifyCartChanged();
}

/** Convenience: add a delta (can be negative) */
export function bumpCartCountInLS(delta: number): void {
  setCartCountInLS(getCartCountFromLS() + delta);
}
