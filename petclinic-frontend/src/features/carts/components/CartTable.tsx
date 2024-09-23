import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import './CartTable.css';

interface CartModel {
  cartId: string;
  customerId: string;
}

export default function CartListTable(): JSX.Element {
  const [carts, setCarts] = useState<CartModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

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
      } finally {
        setLoading(false);
      }
    };

    fetchCarts();
  }, []);



  async function getAllCarts() {
    try {
      const response = await fetch(`http://localhost:8080/api/v2/gateway/carts`, {
        headers: {
          Accept: 'application/json',
        },
        credentials: 'include',
      });

      const carts = await response.json();
      setCarts(carts);
      setLoading(false);

      return carts;
    } catch (error) {
      console.error('Error fetching carts:', error);
      throw error;
    }
  }


  const handleDelete = async (cartId: string) => {
    if (window.confirm('Are you sure you want to delete this cart?')){
      try {
        await fetch(`http://localhost:8080/api/v2/gateway/carts/${cartId}`, {
          method: 'DELETE',
          headers: {
            Accept: 'application/json',
          },
          credentials: 'include',
        });
        console.log('Cart Deleted Successfully')

        const updatedCarts = await getAllCarts();
        setCarts(updatedCarts);
      } catch (err){
        console.error('Error deleting cart', err);
      }
    }
  }

  return (
    <div className="cart-list-container">
      <h1>User Carts</h1>
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
                { <td>
                  <button
                    className="delete-button"
                    onClick={() => handleDelete(cart.cartId)}
                  >
                    Delete
                  </button>
                </td> }
              </tr>
            ))}
          </tbody>
        </table>
      )}
      ?{' '}
    </div>
  );
}
