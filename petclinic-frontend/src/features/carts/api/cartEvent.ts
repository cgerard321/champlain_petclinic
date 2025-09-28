export const CART_CHANGED = 'cart:changed';

export function notifyCartChanged(): void {
  window.dispatchEvent(new Event(CART_CHANGED));
  try {
    localStorage.setItem('cart:changed', String(Date.now()));
  } catch {
    // ignore write failures (private mode, etc.)
  }
}
