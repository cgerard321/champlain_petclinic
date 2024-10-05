import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom'; // Import useNavigate
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';

const UserCart = (): JSX.Element => {
  const { cartId } = useParams<{ cartId: string }>();
  const navigate = useNavigate(); // Initialize useNavigate
  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [wishlistItems, setWishlistItems] = useState<ProductModel[]>([]); // New state for wishlist
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );
  const tvq = subtotal * 0.09975; // Quebec tax rate
  const tvc = subtotal * 0.05; // Canadian tax rate
  const total = subtotal + tvq + tvc;

  useEffect(() => {
    const fetchCartItems = async (): Promise<void> => {
      if (!cartId) {
        setError('Invalid cart ID');
        setLoading(false);
        return;
      }

      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/carts/${cartId}`,
          {
            headers: { Accept: 'application/json' },
            credentials: 'include',
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        const products = data.products.map((product: ProductModel) => ({
          ...product,
          quantity: product.quantity || 1, // Map quantityInCart to quantity
        }));

        setCartItems(products);
        setWishlistItems(data.wishListProducts || []); // Set wishlist items
      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error(err.message); // Log the actual error
          setError('Failed to fetch cart items');
        } else {
          setError('An unexpected error occurred');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchCartItems();
  }, [cartId]);

  const changeItemQuantity = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>, index: number): void => {
      const newQuantity = Math.max(1, +event.target.value); // Ensure quantity is at least 1
      setCartItems(prevItems => {
        const newItems = [...prevItems];
        newItems[index].quantity = newQuantity;
        return newItems;
      });
    },
    []
  );

  const deleteItem = useCallback((indexToDelete: number): void => {
    setCartItems(prevItems =>
      prevItems.filter((_, index) => index !== indexToDelete)
    );
  }, []);

  const clearCart = async (): Promise<void> => {
    if (
      !cartId ||
      !window.confirm('Are you sure you want to clear the cart?')
    ) {
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/carts/${cartId}/clear`,
        {
          method: 'DELETE',
          credentials: 'include',
        }
      );

      if (response.ok) {
        setCartItems([]);
        alert('Cart has been successfully cleared!');
      } else {
        alert('Failed to clear cart');
      }
    } catch (error) {
      console.error('Error clearing cart:', error);
      alert('Failed to clear cart');
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  return (
    <div className="UserCart">
      <NavBar />
      <div className="UserCart-buttons">
        <button className="go-back-btn" onClick={() => navigate(-1)}>
          Go Back
        </button>
        <button className="clear-cart-btn" onClick={clearCart}>
          Clear Cart
        </button>
      </div>
      <hr />

      {/* Main Cart Section */}
      <div className="Cart-section">
        <h2 className="Cart-title">Your Cart</h2> {/* Cart title */}
        <div className="UserCart-items">
          {cartItems.length > 0 ? (
            cartItems.map((item, index) => (
              <CartItem
                key={item.productId}
                item={item}
                index={index}
                changeItemQuantity={changeItemQuantity}
                deleteItem={deleteItem}
              />
            ))
          ) : (
            <p>No products in the cart.</p>
          )}
        </div>
        <div className="CartSummary">
          <h3>Cart Summary</h3>
          <p>Subtotal: ${subtotal.toFixed(2)}</p>
          <p>TVQ (9.975%): ${tvq.toFixed(2)}</p>
          <p>TVC (5%): ${tvc.toFixed(2)}</p>
          <p className="total-price">Total: ${total.toFixed(2)}</p>
        </div>
      </div>

      {/* Wishlist Section */}
      <div className="Wishlist-section">
        <h2>Wishlist</h2>
        <div className="Wishlist-items">
          {wishlistItems.length > 0 ? (
            wishlistItems.map((item, index) => (
              <CartItem
                key={item.productId}
                item={item}
                index={index}
                changeItemQuantity={() => {}} // Disable changing quantity for wishlist
                deleteItem={() => {}} // You may also disable removing from wishlist here
              />
            ))
          ) : (
            <p>No products in the wishlist.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserCart;
