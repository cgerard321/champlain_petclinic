// UserCart.tsx
import { useState, useEffect, useCallback } from 'react';
import CartBillingForm from './CartBillingForm';
import { useParams, useNavigate } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa'; // shopping cart icon
import { IsAdmin } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';

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
  const [errorMessages, setErrorMessages] = useState<Record<number, string>>({});
  const [notificationMessage, setNotificationMessage] = useState<string | null>(null);

  // state: checkout + invoices
  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState<boolean>(false);
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
  const tvc = subtotal * 0.05;    // Canada tax rate
  const total = subtotal - discount + tvq + tvc;

  // recompute badge count whenever cart changes
  const updateCartItemCount = useCallback(() => {
    const count = cartItems.reduce((acc, item) => acc + (item.quantity || 0), 0);
    setCartItemCount(count);
  }, [cartItems]);

  // fetch cart & wishlist
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
      }
    };

    fetchCartItems();
    setWishlistUpdated(false);
    updateCartItemCount();
  }, [cartId, updateCartItemCount, wishlistUpdated]);

  // validate voucher code
  const applyVoucherCode = async (): Promise<void> => {
    try {
      const response = await fetch(
          `http://localhost:8080/api/v2/gateway/promos/validate/${voucherCode}`,
          {
            method: 'GET',
            headers: { Accept: 'application/json' },
            credentials: 'include',
          }
      );

      if (response.ok) {
        const data = await response.json();
        setDiscount((subtotal * data.discount) / 100);
        setVoucherError(null);
      } else {
        setVoucherError('Promo Code Invalid');
      }
    } catch (err) {
      console.error('Error validating voucher code:', err);
      setVoucherError('Error validating voucher code.');
    }
  };

  // change cart quantity w/ stock guard
  const changeItemQuantity = useCallback(
      async (event: React.ChangeEvent<HTMLInputElement>, index: number): Promise<void> => {
        const newQuantity = Math.max(1, Number(event.target.value));
        const item = cartItems[index];

        if (newQuantity > item.productQuantity) {
          setErrorMessages(prev => ({
            ...prev,
            [index]: `You cannot add more than ${item.productQuantity} items. Only ${item.productQuantity} items left in stock.`,
          }));
          return;
        } else {
          setErrorMessages(prev => {
            const copy = { ...prev };
            delete copy[index];
            return copy;
          });
        }

        try {
          const response = await fetch(
              `http://localhost:8080/api/v2/gateway/carts/${cartId}/products/${item.productId}`,
              {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ quantity: newQuantity }),
              }
          );

          const data = await response.json();

          if (!response.ok) {
            setErrorMessages(prev => ({
              ...prev,
              [index]: data.message || 'Failed to update quantity',
            }));

            // if BE moved it to wishlist (e.g., out of stock)
            if (data.message && data.message.includes('moved to your wishlist')) {
              setCartItems(prev => prev.filter((_, idx) => idx !== index));
              setWishlistItems(prev => [...prev, item]);
              setNotificationMessage(data.message);
            }
          } else {
            setCartItems(prev => {
              const next = [...prev];
              next[index].quantity = newQuantity;
              return next;
            });
            setNotificationMessage('Item quantity updated successfully.');
          }
        } catch (err) {
          console.error('Error updating quantity:', err);
          setErrorMessages(prev => ({ ...prev, [index]: 'Failed to update quantity' }));
        }
      },
      [cartItems, cartId]
  );

  // delete line item from cart
  const deleteItem = useCallback(
      async (productId: string, indexToDelete: number): Promise<void> => {
        if (!cartId) return;

        try {
          const response = await fetch(
              `http://localhost:8080/api/v2/gateway/carts/${cartId}/${productId}`,
              {
                method: 'DELETE',
                headers: { Accept: 'application/json' },
                credentials: 'include',
              }
          );

          if (!response.ok) throw new Error('Failed to delete item from the cart');

          setCartItems(prev => prev.filter((_, i) => i !== indexToDelete));
          alert('Item successfully removed!');
        } catch (error) {
          console.error('Error deleting item: ', error);
          alert('Failed to delete item');
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
        const response = await fetch(
            `http://localhost:8080/api/v2/gateway/carts/${cartId}/clear`,
            { method: 'DELETE', credentials: 'include' }
        );

        if (response.ok) {
          setCartItems([]);
          setCartItemCount(0);
          alert('Cart has been successfully cleared!');
        } else {
          throw new Error('Failed to clear cart');
        }
      } catch (error) {
        console.error('Error clearing cart:', error);
        alert('Failed to clear cart');
      }
    }
  };

  // move cart item to wishlist
  const addToWishlist = async (item: ProductModel): Promise<void> => {
    try {
      const productId = item.productId;
      const response = await fetch(
          `http://localhost:8080/api/v2/gateway/carts/${cartId}/wishlist/${productId}/toWishList`,
          {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
              productId: item.productId,
              imageId: item.imageId,
              productName: item.productName,
              productSalePrice: item.productSalePrice,
            }),
          }
      );

      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Failed to add to wishlist');

      setWishlistItems(prev => [...prev, item]);
      if (data.message) setNotificationMessage(data.message);
      else alert(`${item.productName} has been added to your wishlist!`);
      setWishlistUpdated(true);
    } catch (error) {
      console.error('Error adding to wishlist:', error);
      alert('Failed to add item to wishlist.');
    }
  };

  // move wishlist item back to cart
  const addToCartFunction = async (item: ProductModel): Promise<void> => {
    try {
      const productId = item.productId;
      const response = await fetch(
          `http://localhost:8080/api/v2/gateway/carts/${cartId}/wishlist/${productId}/toCart`,
          {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
              productId: item.productId,
              imageId: item.imageId,
              productName: item.productName,
              productSalePrice: item.productSalePrice,
            }),
          }
      );

      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Failed to add to cart');

      setCartItems(prev => [...prev, item]);
      setWishlistItems(prev => prev.filter(p => p.productId !== item.productId));
      if (data.message) setNotificationMessage(data.message);
      else alert(`${item.productName} has been added to your cart!`);
      setWishlistUpdated(true);
    } catch (error) {
      console.error('Error adding to cart:', error);
      alert('Failed to add item to cart.');
    }
  };

  // remove item from wishlist
  const removeFromWishlist = async (item: ProductModel): Promise<void> => {
    if (!cartId) return;

    const ok = window.confirm(`Remove "${item.productName}" from wishlist?`);
    if (!ok) return;

    try {
      const res = await fetch(
          `http://localhost:8080/api/v2/gateway/carts/${cartId}/wishlist/${item.productId}`,
          { method: 'DELETE', headers: { Accept: 'application/json' }, credentials: 'include' }
      );

      if (!res.ok) throw new Error('Failed to remove from wishlist');

      setWishlistItems(prev => prev.filter(p => p.productId !== item.productId));
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
    setShowBillingForm(true);       // <— show billing form first
    setIsCheckoutModalOpen(false);  // confirm comes after form submit
  };

  // POST checkout
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
            headers: { 'Content-Type': 'application/json' },
          }
      );

      if (response.ok) {
        const invoiceItems: Invoice[] = cartItems.map(item => ({
          productId: Number(item.productId),
          productName: item.productName,
          productSalePrice: item.productSalePrice,
          quantity: item.quantity || 1,
        }));

        setInvoices(invoiceItems);
        setCheckoutMessage('Checkout successful! Your order is being processed.');
        setCartItems([]);
        setCartItemCount(0);
        setIsCheckoutModalOpen(false);
      } else {
        const errorData = await response.json();
        setCheckoutMessage(`Checkout failed: ${errorData.message || response.statusText}`);
      }
    } catch (error) {
      console.error('Error during checkout:', error);
      setCheckoutMessage('Checkout failed due to an unexpected error.');
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
                      <span className="cart-badge" aria-label={`Cart has ${cartItemCount} items`}>
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
                <button className="continue-shopping-btn" onClick={() => navigate('/products')}>
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
                    <button onClick={applyVoucherCode} className="apply-voucher-button">
                      Apply
                    </button>
                    {voucherError && <div className="voucher-error">{voucherError}</div>}
                  </div>

                  {/* Summary */}
                  <div className="CartSummary">
                    <h3>Cart Summary</h3>
                    <p className="summary-item">Subtotal: ${subtotal.toFixed(2)}</p>
                    <p className="summary-item">TVQ (9.975%): ${tvq.toFixed(2)}</p>
                    <p className="summary-item">TVC (5%): ${tvc.toFixed(2)}</p>
                    <p className="summary-item">Discount: ${discount.toFixed(2)}</p>
                    <p className="total-price summary-item">Total: ${total.toFixed(2)}</p>
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
                        <button onClick={() => setShowBillingForm(false)}>Cancel</button>
                      </div>
                  )}

                  {/* Confirm (step 2) */}
                  {isCheckoutModalOpen && (
                      <div className="checkout-modal">
                        <h3>Confirm Checkout</h3>
                        <p>Are you sure you want to checkout?</p>
                        <button onClick={handleCheckout}>Yes</button>
                        <button onClick={() => setIsCheckoutModalOpen(false)}>No</button>
                      </div>
                  )}

                  {/* Post-checkout message */}
                  {checkoutMessage && <div className="checkout-message">{checkoutMessage}</div>}

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
                                <p>Total: ${(inv.productSalePrice * inv.quantity).toFixed(2)}</p>
                              </div>
                          ))}
                          <h3>
                            Total: $
                            {invoices
                                .reduce((sum, inv) => sum + inv.productSalePrice * inv.quantity, 0)
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
