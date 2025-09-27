// UserCart.tsx
import { useState, useEffect, useCallback } from 'react';
import CartBillingForm from './CartBillingForm';
import { useParams, useNavigate } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa'; // Importing the shopping cart icon
import axiosInstance from '@/shared/api/axiosInstance';
import { IsAdmin } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';

// NEW: cart change notifier (lets the NavBar update automatically)
import { notifyCartChanged } from '../api/cartEvent';

interface ProductAPIResponse {
  productId: number;
  imageId: string;
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
  const [showBillingForm, setShowBillingForm] = useState<boolean>(false); // Billing form state
  const [wishlistUpdated, setWishlistUpdated] = useState(false);
  const [notificationMessage, setNotificationMessage] = useState<string | null>(
    null
  ); // New state for notifications
  const [voucherCode, setVoucherCode] = useState<string>('');
  const [discount, setDiscount] = useState<number>(0);
  const [voucherError, setVoucherError] = useState<string | null>(null);

  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );
  const tvq = subtotal * 0.09975; // Quebec tax rate
  const tvc = subtotal * 0.05; // Canadian tax rate
  const total = subtotal - discount + tvq + tvc;

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
        const { data } = await axiosInstance.get(`/carts/${cartId}`, {
          useV2: true,
        });

        // Ensure that data.products exists and is an array
        if (!Array.isArray(data.products)) {
          throw new Error('Invalid data format: products should be an array');
        }

        // Map data.products to the appropriate ProductModel format
        const products: ProductModel[] = data.products.map(
          (product: ProductAPIResponse) => ({
            productId: product.productId,
            imageId: product.imageId,
            productName: product.productName,
            productDescription: product.productDescription,
            productSalePrice: product.productSalePrice,
            averageRating: product.averageRating,
            quantity: product.quantityInCart || 1,
            productQuantity: product.productQuantity,
          })
        );

        setCartItems(products);
        const enrichedWishlist = await Promise.all(
          (data.wishListProducts || []).map(async (item: ProductModel) => {
            const fullProduct = await getProductByProductId(item.productId);
            return {
              ...fullProduct,
              quantity: item.quantity ?? 1,
            };
          })
        );
        setWishlistItems(enrichedWishlist);
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

  const applyVoucherCode = async (): Promise<void> => {
    try {
      const { data } = await axiosInstance.get(
        `/promos/validate/${voucherCode}`,
        { useV2: true }
      );
      setDiscount((subtotal * data.discount) / 100);
      setVoucherError(null);
    } catch (err: unknown) {
      console.error('Error validating voucher code:', err);
      setVoucherError('Error validating voucher code.');
    }
  };

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
        const { data } = await axiosInstance.put(
          `/carts/${cartId}/products/${item.productId}`,
          { quantity: newQuantity },
          { useV2: true }
        );

        if (data && data.message) {
          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: data.message || 'Failed to update quantity',
          }));
          // Check if the product has been moved to wishlist
          if (
            typeof data.message === 'string' &&
            data.message.includes('moved to your wishlist')
          ) {
            // Remove the item from cart
            setCartItems(prevItems =>
              prevItems.filter((_, idx) => idx !== index)
            );
            // Add to wishlist
            setWishlistItems(prevItems => [...prevItems, item]);
            setNotificationMessage(data.message);

            //notify navbar (product left cart)
            notifyCartChanged();
            return;
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

          // notify navbar (cart quantity changed)
          notifyCartChanged();
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
        await axiosInstance.delete(`/carts/${cartId}/${productId}`, {
          useV2: true,
        });

        setCartItems(prevItems =>
          prevItems.filter((_, index) => index !== indexToDelete)
        );
        alert('Item successfully removed!');

        // notify navbar (item removed)
        notifyCartChanged();
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
        await axiosInstance.delete(`/carts/${cartId}/clear`, { useV2: true });

        setCartItems([]);
        setCartItemCount(0);
        alert('Cart has been successfully cleared!');

        // notify navbar (cart cleared)
        notifyCartChanged();
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
      const { data } = await axiosInstance.put(
        `/carts/${cartId}/wishlist/${productId}/toWishList`,
        {
          productId: item.productId,
          imageId: item.imageId,
          productName: item.productName,
          productSalePrice: item.productSalePrice,
        },
        { useV2: true }
      );

      if (data && data.message) {
        // Display notification message from backend
        setNotificationMessage(data.message);
      } else {
        alert(`${item.productName} has been added to your wishlist!`);
      }

      // Update wishlist state
      setWishlistItems(prevItems => [...prevItems, item]);

      // Trigger the useEffect by updating the wishlistUpdated state
      setWishlistUpdated(true);

      //notify navbar (item moved out of cart)
      notifyCartChanged();
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
    if (item.productQuantity <= 0) {
      setNotificationMessage(
        `${item.productName} is out of stock and cannot be added to the cart.`
      );
      return;
    }
    try {
      const productId = item.productId;
      const { data } = await axiosInstance.put(
        `/carts/${cartId}/wishlist/${productId}/toCart`,
        {
          productId: item.productId,
          imageId: item.imageId,
          productName: item.productName,
          productSalePrice: item.productSalePrice,
        },
        { useV2: true }
      );

      if (data && data.message) {
        setNotificationMessage(data.message);
      } else {
        alert(`${item.productName} has been added to your cart!`);
      }

      // Update cart items state
      setCartItems(prevItems => [...prevItems, item]);

      // Remove from wishlist
      setWishlistItems(prevItems =>
        prevItems.filter(product => product.productId !== item.productId)
      );

      // Trigger the useEffect by updating the wishlistUpdated state
      setWishlistUpdated(true);

      //notify navbar (item moved into cart)
      notifyCartChanged();
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

  // a function to remove from wishlist
  const removeFromWishlist = async (item: ProductModel): Promise<void> => {
    if (!cartId) return;

    const ok = window.confirm(`Remove "${item.productName}" from wishlist?`);
    if (!ok) return;

    try {
      await axiosInstance.delete(
        `/carts/${cartId}/wishlist/${item.productId}`,
        { useV2: true }
      );

      setWishlistItems(prev =>
        prev.filter(p => p.productId !== item.productId)
      );
      setNotificationMessage(`Removed "${item.productName}" from wishlist.`);
    } catch (e) {
      console.error(e);
      alert('Could not remove item from wishlist.');
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

  // role flag for conditional UI
  const isAdmin = IsAdmin();

  // method modified so admin can't check out anymore
  const handleCheckoutConfirmation = (): void => {
    if (isAdmin) {
      navigate(AppRoutePaths.Unauthorized, {
        state: { message: 'Admins are not allowed to perform checkout.' },
      });
      return;
    }
    // Non-admin flow: open billing form first; on submit we show confirm modal
    setShowBillingForm(true);
    setIsCheckoutModalOpen(false);
  };

  const handleCheckout = async (): Promise<void> => {
    if (!cartId) {
      setCheckoutMessage('Invalid cart ID');
      return;
    }

    try {
      await axiosInstance.post(
        `/carts/${cartId}/checkout`,
        {},
        { useV2: true }
      );

      const invoiceItems: Invoice[] = cartItems.map(item => ({
        productId: Number(item.productId), // Ensure productId is a number
        productName: item.productName,
        productSalePrice: item.productSalePrice,
        quantity: item.quantity || 1,
      }));

      // Set the invoices state
      setInvoices(invoiceItems);

      setCheckoutMessage('Checkout successful! Your order is being processed.');
      setCartItems([]); // Clear the cart after successful checkout
      setCartItemCount(0);
      setIsCheckoutModalOpen(false);

      // notify navbar (cart emptied)
      notifyCartChanged();
    } catch (error: unknown) {
      if (error && typeof error === 'object' && 'response' in error) {
        const errorData = (
          error as { response?: { data?: { message?: string } } }
        ).response?.data;
        setCheckoutMessage(
          `Checkout failed: ${errorData?.message || 'Unexpected error'}`
        );
      } else {
        setCheckoutMessage('Checkout failed: Unexpected error');
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading cart items...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div>
      <NavBar />
      <h2 className="cart-header-title">Your Cart</h2>

      <div className="UserCart-container">
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

        {/* Main Flex Container for Cart and Checkout */}
        <div className="UserCart-checkout-flex">
          {/* Cart Section */}
          <div className="UserCart">
            {/* Cart Header with Badge */}
            <div className="cart-header">
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
                    addToCart={() => {}}
                    isInWishlist={false}
                    showNotification={setNotificationMessage}
                  />
                ))
              ) : (
                <p className="empty-cart-message">No products in the cart.</p>
              )}
            </div>

            {/* Cart Control Buttons */}
            <div className="UserCart-buttons">
              <button
                className="continue-shopping-btn"
                onClick={() => navigate('/products')}
              >
                Continue Shopping
              </button>
              <button className="clear-cart-btn" onClick={clearCart}>
                Clear Cart
              </button>
            </div>
          </div>

          {/* Checkout Section — hidden for admins */}
          {!isAdmin && (
            <div className="Checkout-section">
              {/* Voucher Code Section */}
              <div className="voucher-code-section">
                <input
                  type="text"
                  placeholder="Enter voucher code"
                  value={voucherCode}
                  onChange={e => {
                    setVoucherCode(e.target.value);
                    setVoucherError(null);
                  }}
                  className="voucher-input"
                />
                <button
                  onClick={applyVoucherCode}
                  className="apply-voucher-button"
                >
                  Apply
                </button>
                {voucherError && (
                  <div className="voucher-error">{voucherError}</div>
                )}
              </div>

              <div className="CartSummary">
                <h3>Cart Summary</h3>
                <p className="summary-item">Subtotal: ${subtotal.toFixed(2)}</p>
                <p className="summary-item">TVQ (9.975%): ${tvq.toFixed(2)}</p>
                <p className="summary-item">TVC (5%): ${tvc.toFixed(2)}</p>
                <p className="summary-item">Discount: ${discount.toFixed(2)}</p>
                <p className="total-price summary-item">
                  Total: ${total.toFixed(2)}
                </p>
              </div>

              <button
                className="checkout-btn"
                onClick={handleCheckoutConfirmation}
                disabled={cartItems.length === 0}
              >
                Checkout
              </button>

              {/* Cart Billing Form Modal */}
              {showBillingForm && (
                <div className="checkout-modal">
                  <CartBillingForm
                    onSubmit={() => {
                      setShowBillingForm(false);
                      setIsCheckoutModalOpen(true);
                    }}
                  />
                  <button onClick={() => setShowBillingForm(false)}>
                    Cancel
                  </button>
                </div>
              )}

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

              {/* Invoice Section */}
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
                          {(
                            invoice.productSalePrice * invoice.quantity
                          ).toFixed(2)}
                        </p>
                      </div>
                    ))}
                    <h3>
                      Total: $
                      {invoices
                        .reduce(
                          (total, inv) =>
                            total + inv.productSalePrice * inv.quantity,
                          0
                        )
                        .toFixed(2)}
                    </h3>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Wishlist Section */}
        <div className="wishlist-section">
          <h2>Your Wishlist</h2>
          <div className="Wishlist-items">
            {wishlistItems.length > 0 ? (
              wishlistItems.map(item => (
                <CartItem
                  key={item.productId}
                  item={item}
                  index={-1}
                  changeItemQuantity={() => {}}
                  deleteItem={() => {}}
                  addToWishlist={() => {}}
                  addToCart={addToCartFunction}
                  isInWishlist={true}
                  removeFromWishlist={removeFromWishlist}
                  showNotification={setNotificationMessage}
                />
              ))
            ) : (
              <p>No products in the wishlist.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserCart;
