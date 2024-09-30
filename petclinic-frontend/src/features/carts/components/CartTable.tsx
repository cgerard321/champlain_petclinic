import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './CartTable.css';
import { ProductModel } from '../models/ProductModel';

interface CartModel {
  cartId: string;
  customerId: string;
  products: Array<ProductModel>;
}

export default function CartListTable(): JSX.Element {
  const [carts, setCarts] = useState<CartModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCarts = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/carts`,
          {
            headers: {
              Accept: 'application/json',
            },
            credentials: 'include',
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        setCarts(data);
      } catch (err) {
        console.error('Error fetching carts:', err);
        setError('Failed to fetch carts');
      }
    };

    fetchCarts();
  }, []);

  return (
    <div>
      {error && <div>{error}</div>}
      <table>
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
                <button
                  className="view-button"
                  onClick={() => navigate(`/cart/${cart.cartId}`)}
                >
                  View
                </button>
              </td>
              <td>
                <button
                  className="delete-button"
                  /*onClick={() => deleteCart(cart.cartId)}*/
                >
                  Delete Cart
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
