import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
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

  const getAllCarts = async (): Promise<void> => {
    try {
      const { data } = await axiosInstance.get<CartModel[]>('/carts', {
        useV2: true,
      });
      setCarts(data);
    } catch (err) {
      console.error('Error fetching carts:', err);
      setError('Failed to fetch carts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getAllCarts();
  }, []);

  const handleDelete = async (cartId: string): Promise<void> => {
    if (!window.confirm('Are you sure you want to delete this cart?')) return;
    try {
      await axiosInstance.delete(`/carts/${cartId}`, { useV2: true });
      await getAllCarts(); // refresh list
    } catch (err) {
      console.error('Error deleting cart:', err);
      setError('Failed to delete cart');
    }
  };

  return (
    <div className="cart-list-container">
      {loading && <div className="loading">Loading carts...</div>}
      {error && <div className="error">{error}</div>}
      {!loading && carts.length === 0 && (
        <div className="no-carts">No carts available.</div>
      )}
      {!loading && carts.length > 0 && (
        <table className="cart-table">
          <thead>
            <tr>
              <th>Cart ID</th>
              <th>Customer ID</th>
              <th>View Cart</th>
              <th>Delete Cart</th>
            </tr>
          </thead>
          <tbody>
            {carts.map(cart => (
              <tr key={cart.cartId}>
                <td>{cart.cartId}</td>
                <td>{cart.customerId}</td>
                <td>
                  <Link to={`/carts/${cart.cartId}`} className="view-button">
                    View Cart
                  </Link>
                </td>
                <td>
                  <button
                    className="delete-button"
                    onClick={() => handleDelete(cart.cartId)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
