// UserCart.tsx
import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa'; // Importing the shopping cart icon

interface ProductAPIResponse {
  productId: number;

  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  quantityInCart: number;
  productQuantity: number;
}

interface Invoice {
  productId: number;
  productName: string;
  productSalePrice: number;
  quantity: number;
}

const UserCart = (): JSX.Element => {
  const { cartId } = useParams<{ cartId: string }>();
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [wishlistItems, setWishlistItems] = useState<ProductModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMessages, setErrorMessages] = useState<{ [key: number]: string }>(
    {}
  );
  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);
  const [invoices, setInvoices] = useState<Invoice[]>([]); // State to hold invoice details
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] =
    useState<boolean>(false); // Modal state
  const [wishlistUpdated, setWishlistUpdated] = useState(false);
  const [notificationMessage, setNotificationMessage] = useState<string | null>(
    null
  ); // New state for notifications

  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );
  const tvq = subtotal * 0.09975; // Quebec tax rate
  const tvc = subtotal * 0.05; // Canadian tax rate
  const total = subtotal + tvq + tvc;

  // Function to update the cart item count
  const updateCartItemCount = useCallback(() => {
    const count = cartItems.reduce(
      (acc, item) => acc + (item.quantity || 0),
      0
    );
    setCartItemCount(count);
  }, [cartItems]);

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

        // Ensure that data.products exists and is an array
        if (!Array.isArray(data.products)) {
          throw new Error('Invalid data format: products should be an array');
        }

        // Map data.products to the appropriate ProductModel format
        const products: ProductModel[] = data.products.map(
          (product: ProductAPIResponse) => ({
            productId: product.productId,
            productName: product.productName,
            productDescription: product.productDescription,
            productSalePrice: product.productSalePrice,
            averageRating: product.averageRating,
            quantity: product.quantityInCart || 1,
            productQuantity: product.productQuantity,
          })
        );

        setCartItems(products);
        setWishlistItems(data.wishListProducts || []);
      } catch (err: unknown) {
        // Changed from any to unknown
        if (err instanceof Error) {
          console.error(err.message);
          setError('Failed to fetch cart items');
        } else {
          console.error('An unexpected error occurred');
          setError('An unexpected error occurred');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchCartItems();
    // Reset wishlistUpdated to avoid unnecessary fetches
    setWishlistUpdated(false);
    // Recalculate cart item count after setting cart items
    updateCartItemCount();
  }, [cartId, updateCartItemCount, wishlistUpdated]);

  const changeItemQuantity = useCallback(
    async (
      event: React.ChangeEvent<HTMLInputElement>,
      index: number
    ): Promise<void> => {
      const newQuantity = Math.max(1, Number(event.target.value)); // Ensure quantity is at least 1
      const item = cartItems[index];

      if (newQuantity > item.productQuantity) {
        // Display error message
        setErrorMessages(prevErrors => ({
          ...prevErrors,
          [index]: `You cannot add more than ${item.productQuantity} items. Only ${item.productQuantity} items left in stock.`,
        }));
        return;
      } else {
        // Clear error message
        setErrorMessages(prevErrors => {
          const rest = { ...prevErrors };
          delete rest[index];
          return rest;
        });
      }

      // Update quantity in backend
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/carts/${cartId}/products/${item.productId}`,
          {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              Accept: 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ quantity: newQuantity }),
          }
        );

        const data = await response.json();

        if (!response.ok) {
          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: data.message || 'Failed to update quantity',
          }));
          // Check if the product has been moved to wishlist
          if (data.message && data.message.includes('moved to your wishlist')) {
            // Remove the item from cart
            setCartItems(prevItems =>
              prevItems.filter((_, idx) => idx !== index)
            );
            // Add to wishlist
            setWishlistItems(prevItems => [...prevItems, item]);
            setNotificationMessage(data.message);
          }
        } else {
          // Update local state
          setCartItems(prevItems => {
            const newItems = [...prevItems];
            newItems[index].quantity = newQuantity;
            return newItems;
          });
          // Optionally, display success message
          setNotificationMessage('Item quantity updated successfully.');
        }
      } catch (err: unknown) {
        // Changed from any to unknown
        console.error('Error updating quantity:', err);
        if (err instanceof Error) {
          const errorMessage = err.message || 'Failed to update quantity';
          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: errorMessage,
          }));
        } else {
          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: 'Failed to update quantity',
          }));
        }
      }
    },
    [cartItems, cartId]
  );

  const deleteItem = useCallback(
    async (productId: string, indexToDelete: number): Promise<void> => {
      if (!cartId) {
        console.error('Cart ID is missing');
        return;
      }

      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/carts/${cartId}/${productId}`,
          {
            method: 'DELETE',
            headers: {
              Accept: 'application/json',
            },
            credentials: 'include',
          }
        );
        if (!response.ok) {
          throw new Error('Failed to delete item from the cart');
        }

        setCartItems(prevItems =>
          prevItems.filter((_, index) => index !== indexToDelete)
        );
        alert('Item successfully removed!');
      } catch (error: unknown) {
        // Changed from any to unknown
        console.error('Error deleting item: ', error);
        if (error instanceof Error) {
          alert(`Failed to delete item: ${error.message}`);
        } else {
          alert('Failed to delete item');
        }
      }
    },
    [cartId]
  );

  const clearCart = async (): Promise<void> => {
    if (!cartId) {
      alert('Invalid cart ID');
      return;
    }

    if (window.confirm('Are you sure you want to clear the cart?')) {
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
          setCartItemCount(0);
          alert('Cart has been successfully cleared!');
        } else {
          throw new Error('Failed to clear cart');
        }
      } catch (error: unknown) {
        // Changed from any to unknown
        console.error('Error clearing cart:', error);
        if (error instanceof Error) {
          alert(`Failed to clear cart: ${error.message}`);
        } else {
          alert('Failed to clear cart');
        }
      }
    }
  };

  // Add to Wishlist Function
  const addToWishlist = async (item: ProductModel): Promise<void> => {
    try {
      const productId = item.productId;
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/carts/${cartId}/wishlist/${productId}/toWishList`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({
            productId: item.productId,
            productName: item.productName,
            productSalePrice: item.productSalePrice,
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Failed to add to wishlist');
      }

      // Update wishlist state
      setWishlistItems(prevItems => [...prevItems, item]);

      // Display notification message from backend
      if (data.message) {
        setNotificationMessage(data.message);
      } else {
        alert(`${item.productName} has been added to your wishlist!`);
      }

      // Trigger the useEffect by updating the wishlistUpdated state
      setWishlistUpdated(true);
    } catch (error: unknown) {
      // Changed from any to unknown
      console.error('Error adding to wishlist:', error);
      if (error instanceof Error) {
        alert(error.message || 'Failed to add item to wishlist.');
      } else {
        alert('Failed to add item to wishlist.');
      }
    }
  };

  // Add to Cart Function (from Wishlist)
  const addToCartFunction = async (item: ProductModel): Promise<void> => {
    try {
      const productId = item.productId;
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/carts/${cartId}/wishlist/${productId}/toCart`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({
            productId: item.productId,
            productName: item.productName,
            productSalePrice: item.productSalePrice,
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Failed to add to cart');
      }

      // Update cart items state
      setCartItems(prevItems => [...prevItems, item]);

      // Remove from wishlist
      setWishlistItems(prevItems =>
        prevItems.filter(product => product.productId !== item.productId)
      );

      // Display notification message from backend
      if (data.message) {
        setNotificationMessage(data.message);
      } else {
        alert(`${item.productName} has been added to your cart!`);
      }

      // Trigger the useEffect by updating the wishlistUpdated state
      setWishlistUpdated(true);
    } catch (error: unknown) {
      // Changed from any to unknown
      console.error('Error adding to cart:', error);
      if (error instanceof Error) {
        alert(error.message || 'Failed to add item to cart.');
      } else {
        alert('Failed to add item to cart.');
      }
    }
  };

  useEffect(() => {
    const savedInvoices = localStorage.getItem('invoices');
    if (savedInvoices) {
      setInvoices(JSON.parse(savedInvoices));
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('invoices', JSON.stringify(invoices));
  }, [invoices]);

  const handleCheckoutConfirmation = (): void => {
    setIsCheckoutModalOpen(true);
  };

  const handleCheckout = async (): Promise<void> => {
    if (!cartId) {
      setCheckoutMessage('Invalid cart ID');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/carts/${cartId}/checkout`,
        {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      if (response.ok) {
        const invoiceItems: Invoice[] = cartItems.map(item => ({
          productId: Number(item.productId), // Ensure productId is a number
          productName: item.productName,
          productSalePrice: item.productSalePrice,
          quantity: item.quantity || 1,
        }));

        // Set the invoices state
        setInvoices(invoiceItems);

        setCheckoutMessage('Checkout successful!');
        setCartItems([]); // Clear the cart after successful checkout
        setCartItemCount(0);
        setIsCheckoutModalOpen(false);
      } else {
        const errorData = await response.json();
        setCheckoutMessage(
          `Checkout failed: ${errorData.message || response.statusText}`
        );
      }
    } catch (error) {
      console.error('Error during checkout:', error);
      setCheckoutMessage('Checkout failed due to an unexpected error.');
    }
  };

  if (loading) {
    return <div className="loading">Loading cart items...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="user-cart-container">
      <NavBar />

      <h1 className="cart-title">User Cart</h1>

      {/* Notification Message */}
      {notificationMessage && (
        <div className="notification-message">
          {notificationMessage}
          <button
            className="close-notification"
            onClick={() => setNotificationMessage(null)}
            aria-label="Close notification"
          >
            &times;
          </button>
        </div>
      )}

      {/* Main Content Container */}
      <div className="content-container">
        <div className="UserCart-checkout-flex">
          {/* Main Cart Section */}
          <div className="UserCart">
            {/* Cart Header with Badge */}
            <div className="cart-header">
              <h2 className="cart-header-title">Your Cart</h2>
              <div className="cart-badge-container">
                <FaShoppingCart aria-label="Shopping Cart" />
                {cartItemCount > 0 && (
                  <span
                    className="cart-badge"
                    aria-label={`Cart has ${cartItemCount} items`}
                  >
                    {cartItemCount}
                  </span>
                )}
              </div>
            </div>

            {/* Cart Items */}
            <div className="cart-items-container">
              {cartItems.length > 0 ? (
                cartItems.map((item, index) => (
                  <CartItem
                    key={item.productId}
                    item={item}
                    index={index}
                    changeItemQuantity={changeItemQuantity}
                    deleteItem={deleteItem}
                    errorMessage={errorMessages[index]}
                    addToWishlist={addToWishlist}
                    addToCart={() => {}} // Not needed in cart items
                    isInWishlist={false}
                    showNotification={setNotificationMessage} // Pass the notification handler
                  />
                ))
              ) : (
                <p className="empty-cart-message">No products in the cart.</p>
              )}
            </div>

            {/* Cart Control Buttons */}
            <div className="cart-control-buttons">
              <button className="btn go-back-btn" onClick={() => navigate(-1)}>
                Go Back
              </button>
              <button className="btn clear-cart-btn" onClick={clearCart}>
                Clear Cart
              </button>
            </div>
            <hr />

            {/* Cart Summary */}
            <div className="CartSummary">
              <h3>Cart Summary</h3>
              <p className="summary-item">Subtotal: ${subtotal.toFixed(2)}</p>
              <p className="summary-item">TVQ (9.975%): ${tvq.toFixed(2)}</p>
              <p className="summary-item">TVC (5%): ${tvc.toFixed(2)}</p>
              <p className="total-price summary-item">
                Total: ${total.toFixed(2)}
              </p>
            </div>

            {/* Checkout Button */}
            <button
              className="checkout-btn"
              onClick={handleCheckoutConfirmation}
              disabled={cartItems.length === 0} // Disable if cart is empty
            >
              Checkout
            </button>
            {/* Checkout Confirmation Modal */}
            {isCheckoutModalOpen && (
              <div className="checkout-modal">
                <h3>Confirm Checkout</h3>
                <p>Are you sure you want to checkout?</p>
                <button onClick={handleCheckout}>Yes</button>
                <button onClick={() => setIsCheckoutModalOpen(false)}>
                  No
                </button>
              </div>
            )}
            {checkoutMessage && (
              <div className="checkout-message">{checkoutMessage}</div>
            )}

            {/* Invoice Section - Display a single invoice with a list of items */}
            {invoices.length > 0 && (
              <div className="invoices-section">
                <h2>Invoice</h2>
                <div className="invoice-summary">
                  <h3>Items</h3>
                  {invoices.map(invoice => (
                    <div key={invoice.productId} className="invoice-card">
                      <h4>{invoice.productName}</h4>
                      <p>Price: ${invoice.productSalePrice.toFixed(2)}</p>
                      <p>Quantity: {invoice.quantity}</p>
                      <p>
                        Total: $
                        {(invoice.productSalePrice * invoice.quantity).toFixed(
                          2
                        )}
                      </p>
                    </div>
                  ))}
                  <h3>
                    Total: $
                    {invoices
                      .reduce(
                        (total, invoice) =>
                          total + invoice.productSalePrice * invoice.quantity,
                        0
                      )
                      .toFixed(2)}
                  </h3>
                </div>
              </div>
            )}

            {/* Wishlist Section */}
            <div className="wishlist-section">
              <h2 className="wishlist-title">Your Wishlist</h2>
              <div className="wishlist-items-container">
                {wishlistItems.length > 0 ? (
                  wishlistItems.map(item => (
                    <CartItem
                      key={item.productId}
                      item={item}
                      index={-1}
                      changeItemQuantity={() => {}}
                      deleteItem={() => {}}
                      addToWishlist={() => {}}
                      addToCart={addToCartFunction} // Use the updated addToCart function
                      isInWishlist={true}
                      showNotification={setNotificationMessage} // Pass the notification handler
                    />
                  ))
                ) : (
                  <p className="empty-wishlist-message">
                    No products in the wishlist.
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserCart;
