import { useCallback, useEffect, useMemo, useRef, useState } from 'react';import './cart-shared.css';
import './CartTable.css';
import axiosInstance from '@/shared/api/axiosInstance';

interface CartModel {
  cartId: string;
  customerId: string;
  customerName?: string;
  products: Array<CartProductModel>;
}

interface CartDetailsModel {
  cartId: string;
  customerId: string;
  customerName?: string;
  products: Array<CartProductModel>;
  wishListProducts?: Array<CartProductModel>;
  subtotal: number;
  tvq: number;
  tvc: number;
  total: number;
  message?: string;
}

interface CartProductModel {
  productId: string;
  productName: string;
  productSalePrice: number;
  quantityInCart: number;
}

interface CustomerDTO {
    customerId: string;
    firstName?: string;
    lastName?: string;
}



export default function CartListTable(): JSX.Element {
  const [carts, setCarts] = useState<CartModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

    const nameCacheRef = useRef<Record<string, string>>({});



    const [selectedCartId, setSelectedCartId] = useState<string | null>(null);
  const [selectedCart, setSelectedCart] = useState<CartDetailsModel | null>(null);
  const [modalLoading, setModalLoading] = useState(false);

  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);


    const fetchCustomerName = useCallback(async (customerId: string): Promise<string | null> => {
        if (!customerId) return null;

        const cached = nameCacheRef.current[customerId];
        if (cached) return cached;

        try {
            const { data } = await axiosInstance.get<CustomerDTO>(`/customers/${customerId}`, { useV2: false });
            const full = `${data.firstName?.trim() ?? ''} ${data.lastName?.trim() ?? ''}`.trim();
            if (full) {
                nameCacheRef.current = { ...nameCacheRef.current, [customerId]: full };
                return full;
            }
        } catch {
        }
        return null;
    }, []);

    const openModal = useCallback(
        async (cartId: string) => {
            setSelectedCartId(cartId);
            setSelectedCart(null);
            setModalLoading(true);
            try {
                const { data } = await axiosInstance.get<CartDetailsModel>(`/carts/${cartId}`, { useV2: false });

                if ((!data.customerName || !data.customerName.trim()) && data.customerId) {
                    const full = await fetchCustomerName(data.customerId);
                    if (full) data.customerName = full;
                }

                setSelectedCart(data);
            } catch (e) {
                console.error('Failed to load cart details', e);
                setSelectedCart(null);
            } finally {
                setModalLoading(false);
            }
        },
        [fetchCustomerName]
    );

    const closeModal = useCallback(() => {
        setSelectedCartId(null);
        setSelectedCart(null);
        setModalLoading(false);
        setActionLoadingId(null);
    }, []);


    const updateLineQty = useCallback(
      async (cartId: string, productId: string, nextQty: number) => {
        if (!selectedCart) return;
        if (nextQty <= 0) return;
        try {
          setActionLoadingId(productId);
          await axiosInstance.put(
              `/carts/${cartId}/products/${productId}`,
              { quantity: nextQty },
              { useV2: false }
          );

          setSelectedCart(prev =>
              prev
                  ? {
                    ...prev,
                    products: prev.products.map(p =>
                        p.productId === productId ? { ...p, quantityInCart: nextQty } : p
                    ),
                    subtotal: prev.products.reduce(
                        (sum, p) =>
                            sum +
                            (p.productId === productId ? nextQty : p.quantityInCart) * p.productSalePrice,
                        0
                    ),
                    tvq: prev.tvq,
                    tvc: prev.tvc,
                    total: prev.total,
                  }
                  : prev
          );

          const { data } = await axiosInstance.get<CartDetailsModel>(`/carts/${cartId}`, { useV2: false });
          setSelectedCart(data);
        } catch (e) {
          console.error('Failed to update quantity', e);
        } finally {
          setActionLoadingId(null);
        }
      },
      [selectedCart]
  );

  const removeLine = useCallback(
      async (cartId: string, productId: string) => {
        try {
          setActionLoadingId(productId);
          await axiosInstance.delete(`/carts/${cartId}/${productId}`, { useV2: false });

          setSelectedCart(prev =>
              prev ? { ...prev, products: prev.products.filter(p => p.productId !== productId) } : prev
          );

          const { data } = await axiosInstance.get<CartDetailsModel>(`/carts/${cartId}`, { useV2: false });
          setSelectedCart(data);
        } catch (e) {
          console.error('Failed to remove product', e);
        } finally {
          setActionLoadingId(null);
        }
      },
      []
  );

  const cartExtractor = useMemo(
    () =>
      (payload: unknown): CartModel[] => {
        if (Array.isArray(payload)) {
          return payload as CartModel[];
        }

        if (typeof payload === 'string') {
          const trimmed = payload.trim();
          if (trimmed.length === 0) return [];

          if (trimmed.startsWith('[') || trimmed.startsWith('{')) {
            try {
              const parsed = JSON.parse(trimmed) as unknown;
              return Array.isArray(parsed)
                ? (parsed as CartModel[])
                : parsed
                  ? [parsed as CartModel]
                  : [];
            } catch {}
          }

          const chunks = trimmed
            .split(/\r?\n/)
            .map(line => line.trim())
            .filter(line => line.startsWith('data:'))
            .map(line => line.slice(5).trim())
            .filter(Boolean);

          const carts: CartModel[] = [];
          for (const chunk of chunks) {
            try {
              carts.push(JSON.parse(chunk) as CartModel);
            } catch {}
          }
          if (carts.length > 0) return carts;
        }

        if (payload && typeof payload === 'object') {
          const possibleArrays = [
            (payload as { carts?: unknown }).carts,
            (payload as { content?: unknown }).content,
            (payload as { data?: unknown }).data,
          ];
          for (const candidate of possibleArrays) {
            if (Array.isArray(candidate)) return candidate as CartModel[];
          }
        }

        return [];
      },
    []
  );



    const fetchCarts = useCallback(async (): Promise<void> => {
        try {
            setLoading(true);

            const { data } = await axiosInstance.get<CartModel[] | string | Record<string, unknown>>(
                '/carts/list',
                { useV2: false, headers: { Accept: 'application/json' } }
            );

            const normalized = cartExtractor(data);

            const allHaveNames = normalized.every(c => c.customerName && c.customerName.trim());
            if (allHaveNames) {
                setCarts(normalized);
                setLoading(false);
                return;
            }

            const idsNeedingNames = Array.from(
                new Set(normalized.filter(c => !c.customerName && !!c.customerId).map(c => c.customerId))
            );

            const namePairs = await Promise.all(
                idsNeedingNames.map(async id => {
                    const name = await fetchCustomerName(id);
                    return name ? ([id, name] as const) : null;
                })
            );

            const fetchedNames: Record<string, string> = {};
            for (const pair of namePairs) if (pair) fetchedNames[pair[0]] = pair[1];

            if (Object.keys(fetchedNames).length) {
                nameCacheRef.current = { ...nameCacheRef.current, ...fetchedNames };
            }


            const withNames = normalized.map(c =>
                c.customerName || !fetchedNames[c.customerId]
                    ? c
                    : { ...c, customerName: fetchedNames[c.customerId] }
            );

            setCarts(withNames);
        } catch (err) {
            console.error('Error fetching carts:', err);
            setError('Failed to fetch carts');
        } finally {
            setLoading(false);
        }
    }, [cartExtractor, fetchCustomerName]); // both are now stable


    useEffect(() => {
        void fetchCarts();
    }, [fetchCarts]);


    return (
    <div className="cart-list-container cart-panel cart-panel--spacious">
      {loading && (
        <div className="loading cart-panel cart-panel--padded">
          Loading carts...
        </div>
      )}
      {error && (
        <div className="error cart-panel cart-panel--padded">{error}</div>
      )}
      {!loading && carts.length === 0 && (
        <div className="no-carts cart-panel cart-panel--padded">
          No carts available.
        </div>
      )}
      {!loading && carts.length > 0 && (
        <table className="cart-table">
          <thead>
          <tr>
            <th>Cart ID</th>
            <th>Customer</th>
            <th>View</th>
          </tr>
          </thead>
          <tbody>
          {carts.map(cart => (
              <tr key={cart.cartId}>
                <td>{cart.cartId}</td>
                <td>{cart.customerName?.trim() || cart.customerId}</td>
                <td>
                  <button
                      type="button"
                      onClick={() => openModal(cart.cartId)}
                      className="cart-button cart-button--brand"
                  >
                    View
                  </button>
                </td>
              </tr>
          ))}
          </tbody>

        </table>
      )}


      {selectedCartId && (
          <div className="cart-modal-backdrop">
            <div className="cart-modal">
              <div className="cart-modal__header">
                <h2>
                  Cart {selectedCartId} —{' '}
                  {selectedCart?.customerName?.trim() || selectedCart?.customerId || 'Loading…'}
                </h2>
                <button type="button" className="cart-button" onClick={closeModal}>
                  Close
                </button>
              </div>

              {modalLoading && (
                  <div className="cart-panel cart-panel--padded">Loading cart…</div>
              )}

              {!modalLoading && selectedCart && (
                  <div className="cart-modal__body">
                    <div className="cart-summary">
                      <div>Subtotal: {selectedCart.subtotal.toFixed(2)}</div>
                      <div>TVQ: {selectedCart.tvq.toFixed(2)}</div>
                      <div>TVC: {selectedCart.tvc.toFixed(2)}</div>
                      <div>
                        <strong>Total: {selectedCart.total.toFixed(2)}</strong>
                      </div>
                    </div>

                    <table className="cart-table cart-table--compact">
                      <thead>
                      <tr>
                        <th>Product</th>
                        <th>Qty</th>
                        <th>Unit $</th>
                        <th>Line $</th>
                        <th>Action</th> {/* ADDED */}
                      </tr>
                      </thead>
                      <tbody>
                      {selectedCart.products?.map(p => {
                        const disabled = actionLoadingId === p.productId || modalLoading;
                        const nextMinus = Math.max(1, p.quantityInCart - 1);
                        const nextPlus = p.quantityInCart + 1;

                        return (
                            <tr key={p.productId}>
                              <td>{p.productName}</td>
                              <td>
                                <div className="qty-controls">
                                  <button
                                      type="button"
                                      className="cart-button cart-button--ghost"
                                      disabled={disabled || p.quantityInCart <= 1}
                                      onClick={() =>
                                          updateLineQty(selectedCart.cartId, p.productId, nextMinus)
                                      }
                                  >
                                    −
                                  </button>
                                  <span className="qty-value">{p.quantityInCart}</span>
                                  <button
                                      type="button"
                                      className="cart-button cart-button--ghost"
                                      disabled={disabled}
                                      onClick={() =>
                                          updateLineQty(selectedCart.cartId, p.productId, nextPlus)
                                      }
                                  >
                                    +
                                  </button>
                                </div>
                              </td>
                              <td>{p.productSalePrice?.toFixed(2)}</td>
                              <td>{(p.productSalePrice * p.quantityInCart).toFixed(2)}</td>
                              <td>
                                <button
                                    type="button"
                                    className="cart-button cart-button--danger"
                                    disabled={disabled}
                                    onClick={() => removeLine(selectedCart.cartId, p.productId)}
                                >
                                  Remove
                                </button>
                              </td>
                            </tr>
                        );
                      })}
                      {(!selectedCart.products || selectedCart.products.length === 0) && (
                          <tr>
                            <td colSpan={5} style={{ textAlign: 'center' }}>
                              No items in cart.
                            </td>
                          </tr>
                      )}
                      </tbody>
                    </table>
                  </div>
              )}
            </div>
          </div>
      )}
    </div>
  );
}
