import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import './cart-shared.css';
import './CartTable.css';
import { ProductModel } from '../models/ProductModel';
import axiosInstance from '@/shared/api/axiosInstance';

interface CartModel {
  cartId: string;
  customerId: string;
  products: Array<ProductModel>;
}

export default function CartListTable(): JSX.Element {
  const [carts, setCarts] = useState<CartModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

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
      const { data } = await axiosInstance.get<
        CartModel[] | string | Record<string, unknown>
      >('/carts', {
        useV2: false,
      });
      const normalized = cartExtractor(data);

      if (!Array.isArray(data) && normalized.length === 0) {
        console.warn('Unexpected carts payload shape. Received:', data);
      }

      setCarts(normalized);
    } catch (err) {
      console.error('Error fetching carts:', err);
      setError('Failed to fetch carts');
    } finally {
      setLoading(false);
    }
  }, [cartExtractor]);

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
              <th>Customer ID</th>
              <th>View Cart</th>
            </tr>
          </thead>
          <tbody>
            {carts.map(cart => (
              <tr key={cart.cartId}>
                <td>{cart.cartId}</td>
                <td>{cart.customerId}</td>
                <td>
                  <Link
                    to={`/carts/${cart.cartId}`}
                    className="cart-button cart-button--brand"
                  >
                    View Cart
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
