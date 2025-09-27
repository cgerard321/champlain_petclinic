// UserCart.tsx
import { useState, useEffect, useCallback } from 'react';
import CartBillingForm from './CartBillingForm';
import { useParams, useNavigate } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa'; // shopping cart icon
import axiosInstance from '@/shared/api/axiosInstance';
import { IsAdmin } from '@/context/UserContext';

import { notifyCartChanged } from '../api/cartEvent';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

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
  // router + nav
  const { cartId } = useParams<{ cartId: string }>();
  const navigate = useNavigate();

  // state: cart + wishlist
  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [wishlistItems, setWishlistItems] = useState<ProductModel[]>([]);

  // state: ui + error/loading
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMessages, setErrorMessages] = useState<Record<number, string>>(
    {}
  );
  const [notificationMessage, setNotificationMessage] = useState<string | null>(
    null
  );

  // state: checkout + invoices
  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] =
    useState<boolean>(false);
  const [showBillingForm, setShowBillingForm] = useState<boolean>(false);

  // state: misc
  const [wishlistUpdated, setWishlistUpdated] = useState(false);
  const [voucherCode, setVoucherCode] = useState<string>('');
  const [discount, setDiscount] = useState<number>(0);
  const [voucherError, setVoucherError] = useState<string | null>(null);

  // derived totals
  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );
  const tvq = subtotal * 0.09975; // Quebec tax rate
  const tvc = subtotal * 0.05; // Canada tax rate
  const total = subtotal - discount + tvq + tvc;

  // recompute badge count whenever cart changes
  const updateCartItemCount = useCallback(() => {
    const count = cartItems.reduce(
      (acc, item) => acc + (item.quantity || 0),
      0
    );
    setCartItemCount(count);
  }, [cartItems]);

  // Fetch cart items when cartId or wishlistUpdated changes
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

        if (!Array.isArray(data.products)) {
          throw new Error('Invalid data format: products should be an array');
        }

        // map API payload to ProductModel
        const products: ProductModel[] = data.products.map(
          (p: ProductAPIResponse) => ({
            productId: p.productId,
            imageId: p.imageId,
            productName: p.productName,
            productDescription: p.productDescription,
            productSalePrice: p.productSalePrice,
            averageRating: p.averageRating,
            quantity: p.quantityInCart || 1,
            productQuantity: p.productQuantity,
          })
        );

        setCartItems(products);
        setWishlistItems(data.wishListProducts || []);
      } catch (err: unknown) {
        console.error(err);
        setError('Failed to fetch cart items');
      } finally {
        setLoading(false);
        if (wishlistUpdated) setWishlistUpdated(false);
      }
    };

    fetchCartItems();
  }, [cartId, wishlistUpdated]);

  // Update cart item count whenever cartItems changes
  useEffect(() => {
    updateCartItemCount();
  }, [cartItems, updateCartItemCount]);

  // validate voucher code
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

  // change cart quantity w/ stock guard
  const changeItemQuantity = useCallback(
    async (
      event: React.ChangeEvent<HTMLInputElement>,
      index: number
    ): Promise<void> => {
      const newQuantity = Math.max(1, Number(event.target.value));
      const item = cartItems[index];

      if (newQuantity > item.productQuantity) {
        setErrorMessages(prevErrors => ({
          ...prevErrors,
          [index]: `You cannot add more than ${item.productQuantity} items. Only ${item.productQuantity} items left in stock.`,
        }));
        return;
      } else {
        setErrorMessages(prevErrors => {
          const rest = { ...prevErrors };
          delete rest[index];
          return rest;
        });
      }

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

          if (
            typeof data.message === 'string' &&
            data.message.includes('moved to your wishlist')
          ) {
            setCartItems(prevItems =>
              prevItems.filter((_, idx) => idx !== index)
            );
            setWishlistItems(prevItems => [...prevItems, item]);
            setNotificationMessage(data.message);
            notifyCartChanged(); // left cart
            return;
          }
        } else {
          setCartItems(prev => {
            const next = [...prev];
            next[index] = { ...next[index], quantity: newQuantity };
            return next;
          });
          setNotificationMessage('Item quantity updated successfully.');
          notifyCartChanged(); // qty changed
        }
      } catch (err) {
        console.error('Error updating quantity:', err);
        setErrorMessages(prev => ({
          ...prev,
          [index]: 'Failed to update quantity',
        }));
      }
    },
    [cartItems, cartId]
  );

  const deleteItem = useCallback(
    async (productId: string, indexToDelete: number): Promise<void> => {
      if (!cartId) return;

      try {
        await axiosInstance.delete(`/carts/${cartId}/${productId}`, {
          useV2: true,
        });

        setCartItems(prevItems =>
          prevItems.filter((_, index) => index !== indexToDelete)
        );
        alert('Item successfully removed!');
        notifyCartChanged(); // item removed
      } catch (error: unknown) {
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

  // clear entire cart
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
        alert('Failed to clear cart');
      }
    }
  };

  // move cart item to wishlist
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
      alert('Failed to add item to wishlist.');
    }
  };

  // move wishlist item back to cart
  const addToCartFunction = async (item: ProductModel): Promise<void> => {
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
      alert('Failed to add item to cart.');
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

  // persist invoices locally
  useEffect(() => {
    const saved = localStorage.getItem('invoices');
    if (saved) setInvoices(JSON.parse(saved));
  }, []);
  useEffect(() => {
    localStorage.setItem('invoices', JSON.stringify(invoices));
  }, [invoices]);

  // role guard
  const isAdmin = IsAdmin();

  const handleCheckoutConfirmation = (): void => {
    if (isAdmin) {
      navigate(AppRoutePaths.Unauthorized, {
        state: { message: 'Admins are not allowed to perform checkout.' },
      });
      return;
    }
    // show billing form first
    setShowBillingForm(true);
    // confirm comes after form submit
    setIsCheckoutModalOpen(false);
  };

  // POST checkout
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

  // early returns
  if (loading) return <div className="loading">Loading cart items...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div>
      <NavBar />
      <h2 className="cart-header-title">Your Cart</h2>

      <div className="UserCart-container">
        {/* Notification banner */}
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

        {/* Main layout: cart (left) + summary/checkout (right) */}
        <div className="UserCart-checkout-flex">
          {/* Cart Column */}
          <div className="UserCart">
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

            {/* Items */}
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

            {/* Actions */}
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

          {/* Checkout Column (hidden for admins) */}
          {!isAdmin && (
            <div className="Checkout-section">
              {/* Voucher */}
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

              {/* Summary */}
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

              {/* Checkout CTA */}
              <button
                className="checkout-btn"
                onClick={handleCheckoutConfirmation}
                disabled={cartItems.length === 0}
              >
                Checkout
              </button>

              {/* Billing Form (step 1) */}
              {showBillingForm && (
                <div className="checkout-modal">
                  <CartBillingForm
                    onSubmit={() => {
                      setShowBillingForm(false);
                      setIsCheckoutModalOpen(true); // proceed to confirmation
                    }}
                  />
                  <button onClick={() => setShowBillingForm(false)}>
                    Cancel
                  </button>
                </div>
              )}

              {/* Confirm (step 2) */}
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

              {/* Post-checkout message */}
              {checkoutMessage && (
                <div className="checkout-message">{checkoutMessage}</div>
              )}

              {/* Invoice */}
              {invoices.length > 0 && (
                <div className="invoices-section">
                  <h2>Invoice</h2>
                  <div className="invoice-summary">
                    <h3>Items</h3>
                    {invoices.map(inv => (
                      <div key={inv.productId} className="invoice-card">
                        <h4>{inv.productName}</h4>
                        <p>Price: ${inv.productSalePrice.toFixed(2)}</p>
                        <p>Quantity: {inv.quantity}</p>
                        <p>
                          Total: $
                          {(inv.productSalePrice * inv.quantity).toFixed(2)}
                        </p>
                      </div>
                    ))}
                    <h3>
                      Total: $
                      {invoices
                        .reduce(
                          (sum, inv) =>
                            sum + inv.productSalePrice * inv.quantity,
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

        {/* Wishlist */}
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
