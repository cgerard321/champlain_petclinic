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

interface InvoiceItem {
  productId: string;
  productName: string;
  productSalePrice: number;
  quantity: number;
}

interface Invoice {
  invoiceId: string;
  cartId: string;
  items: InvoiceItem[];
  subtotal: number;
  tax: number;
  total: number;
  issueDate: string;
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
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [cartItemCount, setCartItemCount] = useState<number>(0); // State for cart item count

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
        if (err instanceof Error) {
          console.error(err.message);
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

  // Recalculate cart item count every time cartItems change
  useEffect(() => {
    updateCartItemCount();
  }, [cartItems, updateCartItemCount]);

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

        if (!response.ok) {
          const errorData = await response.json();
          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: errorData.message || 'Failed to update quantity',
          }));
        } else {
          // Update local state
          setCartItems(prevItems => {
            const newItems = [...prevItems];
            newItems[index].quantity = newQuantity;
            return newItems;
          });
        }
      } catch (err) {
        console.error('Error updating quantity:', err);
        setErrorMessages(prevErrors => ({
          ...prevErrors,
          [index]: 'Failed to update quantity',
        }));
      }
    },
    [cartItems, cartId]
  );

  const deleteItem = useCallback((indexToDelete: number): void => {
    setCartItems(prevItems => {
      return prevItems.filter((_, index) => index !== indexToDelete);
    });
  }, []);

  const clearCart = async (): Promise<void> => {
    if (!cartId) {
      alert('Invalid cart ID');
      return;
    }

    if (!window.confirm('Are you sure you want to clear the cart?')) {
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
        setCartItemCount(0);
        alert('Cart has been successfully cleared!');
      } else {
        alert('Failed to clear cart');
      }
    } catch (error) {
      console.error('Error clearing cart:', error);
      alert('Failed to clear cart');
    }
  };

  //going to imlement it next sprint
  // const addToWishlist = async (item: ProductModel): Promise<void> => {
  //   try {
  //     const productId = item.productId;
  //     const response = await fetch(
  //       `http://localhost:8080/api/v2/gateway/carts/${cartId}/products/${productId}/toWishList`,
  //       {
  //         method: 'POST',
  //         headers: {
  //           'Content-Type': 'application/json',
  //           Accept: 'application/json',
  //         },
  //         credentials: 'include',
  //         body: JSON.stringify({
  //           productId: item.productId,
  //           productName: item.productName,
  //           productSalePrice: item.productSalePrice,
  //         }),
  //       }
  //     );

  //     if (!response.ok) {
  //       const errorData = await response.json();
  //       throw new Error(errorData.message || 'Failed to add to wishlist');
  //     }

  //     // Optionally, you can update the wishlistItems state
  //     setWishlistItems(prevItems => [...prevItems, item]);
  //     alert(`${item.productName} has been added to your wishlist!`);
  //   } catch (error) {
  //     console.error('Error adding to wishlist:', error);
  //     alert('Failed to add item to wishlist.');
  //   }
  // };

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
        // Generate the invoice
        const newInvoice: Invoice = {
          invoiceId: 'INV-' + new Date().getTime(), // Generate a simple invoice ID
          cartId,
          items: cartItems.map(item => ({
            productId: item.productId,
            productName: item.productName,
            productSalePrice: item.productSalePrice,
            quantity: item.quantity || 1,
          })),
          subtotal,
          tax: tvq + tvc,
          total,
          issueDate: new Date().toISOString(), // Current date
        };

        setInvoice(newInvoice); // Set the new invoice state
        setCheckoutMessage('Checkout successful!'); // Notify the user
        setCartItems([]); // Clear cart after checkout
        setCartItemCount(0);
      } else {
        setCheckoutMessage('Checkout failed.');
      }
    } catch (error) {
      console.error('Error during checkout:', error);
      setCheckoutMessage('Checkout failed.');
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

            {/* Checkout Section */}
            <h3>Checkout</h3>
            <button className="btn checkout-btn" onClick={handleCheckout}>
              Checkout
            </button>
            {checkoutMessage && (
              <div className="checkout-message">{checkoutMessage}</div>
            )}

            {/* Invoice Section */}
            {invoice && (
              <div className="invoice-section">
                <h2 className="invoice-title">Invoice Details</h2>
                <p className="invoice-id">Invoice ID: {invoice.invoiceId}</p>
                <p className="cart-id">Cart ID: {invoice.cartId}</p>
                <p className="invoice-subtotal">
                  Subtotal: ${invoice.subtotal.toFixed(2)}
                </p>
                <p className="invoice-tax">Tax: ${invoice.tax.toFixed(2)}</p>
                <p className="invoice-total">
                  Total: ${invoice.total.toFixed(2)}
                </p>
                <p className="invoice-date">
                  Issue Date: {new Date(invoice.issueDate).toLocaleString()}
                </p>

                {/* Invoice Items */}
                <h3 className="invoice-items-title">Items:</h3>
                <ul className="invoice-items-list">
                  {invoice.items.map((item, index) => (
                    <li key={index} className="invoice-item">
                      {item.productName} - Quantity: {item.quantity} - Price: $
                      {item.productSalePrice.toFixed(2)}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          {/* Wishlist Section */}
          <div className="wishlist-section">
            <h2 className="wishlist-title">Your Wishlist</h2>
            <div className="wishlist-items-container">
              {wishlistItems.length > 0 ? (
                wishlistItems.map(item => (
                  <CartItem
                    key={item.productId}
                    item={item}
                    index={-1} // Mark as wishlist item
                    changeItemQuantity={() => {}} // Disable changing quantity for wishlist
                    deleteItem={() => {}} // Disable removing from wishlist
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
  );
};

export default UserCart;
