import { useState, useEffect, useCallback } from 'react';
import CartBillingForm, { BillingInfo } from './CartBillingForm';
import { useParams, useNavigate } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa';
import axiosInstance from '@/shared/api/axiosInstance';
import {
  IsAdmin,
  IsInventoryManager,
  IsVet,
  IsReceptionist,
} from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';
import { notifyCartChanged } from '../api/cartEvent';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';

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

  const { confirm, ConfirmModal } = useConfirmModal();

  // state: cart + wishlist
  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [wishlistItems, setWishlistItems] = useState<ProductModel[]>([]);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMessages, setErrorMessages] = useState<Record<number, string>>(
    {}
  );
  const [notificationMessage, setNotificationMessage] = useState<string | null>(
    null
  );

  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] =
    useState<boolean>(false);
  const [showBillingForm, setShowBillingForm] = useState<boolean>(false);
  const [billingInfo, setBillingInfo] = useState<BillingInfo | null>(null);
  const [checkoutDate, setCheckoutDate] = useState<string | null>(null);

  const [wishlistUpdated, setWishlistUpdated] = useState(false);
  const [voucherCode, setVoucherCode] = useState<string>('');
  const [discount, setDiscount] = useState<number>(0);
  const [voucherError, setVoucherError] = useState<string | null>(null);

  const [movingAll, setMovingAll] = useState<boolean>(false);

  // rôles (read-only pour staff/admin)
  const isAdmin = IsAdmin();
  const isStaff =
    isAdmin || IsInventoryManager() || IsVet() || IsReceptionist();

  // derived totals
  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );
  const tvq = subtotal * 0.09975;
  const tvc = subtotal * 0.05;
  const total = subtotal - discount + tvq + tvc;

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

        if (!Array.isArray(data.products)) {
          throw new Error('Invalid data format: products should be an array');
        }

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
        console.error(err);
        setError('Failed to fetch cart items');
      } finally {
        setLoading(false);
        if (wishlistUpdated) setWishlistUpdated(false);
      }
    };

    fetchCartItems();
  }, [cartId, wishlistUpdated]);

  useEffect(() => {
    updateCartItemCount();
  }, [cartItems, updateCartItemCount]);

  const applyVoucherCode = async (): Promise<void> => {
    try {
      const { data } = await axiosInstance.get(
        `/promos/validate/${voucherCode}`,
        { useV2: false }
      );
      setDiscount((subtotal * data.discount) / 100);
      setVoucherError(null);
    } catch (err: unknown) {
      console.error('Error validating voucher code:', err);
      setVoucherError('Error validating voucher code.');
    }
  };

  const blockIfReadOnly = useCallback((): boolean => {
    if (isStaff) {
      setNotificationMessage(
        'Read-only mode: staff/admin cannot modify carts.'
      );
      return true;
    }
    return false;
  }, [isStaff, setNotificationMessage]);

  const changeItemQuantity = useCallback(
    async (
      event: React.ChangeEvent<HTMLInputElement>,
      index: number
    ): Promise<void> => {
      if (blockIfReadOnly()) return;

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
          { useV2: false }
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
            notifyCartChanged();
            return;
          }
        } else {
          setCartItems(prev => {
            const next = [...prev];
            next[index] = { ...next[index], quantity: newQuantity };
            return next;
          });
          setNotificationMessage('Item quantity updated successfully.');
          notifyCartChanged();
        }
      } catch (err) {
        console.error('Error updating quantity:', err);
        setErrorMessages(prev => ({
          ...prev,
          [index]: 'Failed to update quantity',
        }));
      }
    },
    [cartItems, cartId, blockIfReadOnly]
  );

  const deleteItem = useCallback(
    async (productId: string, indexToDelete: number): Promise<void> => {
      if (blockIfReadOnly()) return;
      if (!cartId) return;

      const ok = await confirm({
        title: 'Remove item',
        message: 'Remove this item from your cart?',
        confirmText: 'Remove',
        cancelText: 'Cancel',
        variant: 'danger',
      });
      if (!ok) return;

      try {
        await axiosInstance.delete(`/carts/${cartId}/${productId}`, {
          useV2: false,
        });

        setCartItems(prev => prev.filter((_, idx) => idx !== indexToDelete));
        notifyCartChanged();
      } catch (error) {
        console.error('Error deleting item:', error);
        setNotificationMessage('Failed to delete item.');
      }
    },
    [cartId, confirm, blockIfReadOnly]
  );

  const clearCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;

    if (!cartId) {
      setNotificationMessage('Invalid cart ID');
      return;
    }

    const ok = await confirm({
      title: 'Clear cart',
      message: 'Are you sure you want to clear the cart?',
      confirmText: 'Clear cart',
      cancelText: 'Cancel',
      variant: 'danger',
    });
    if (!ok) return;

    try {
      await axiosInstance.delete(`/carts/${cartId}/clear`, { useV2: false });
      setCartItems([]);
      setCartItemCount(0);
      notifyCartChanged();
    } catch (error) {
      console.error('Error clearing cart:', error);
      setNotificationMessage('Failed to clear cart.');
    }
  };

  const addToWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

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
        { useV2: false }
      );

      if (data && data.message) {
        setNotificationMessage(data.message);
      } else {
        setNotificationMessage(
          `${item.productName} has been added to your wishlist!`
        );
      }

      setWishlistItems(prevItems => [...prevItems, item]);
      setWishlistUpdated(true);
      notifyCartChanged();
    } catch (error: unknown) {
      console.error('Error adding to wishlist:', error);
      alert('Failed to add item to wishlist.');
    }
  };

  const addToCartFunction = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

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
        { useV2: false }
      );

      if (data && data.message) {
        setNotificationMessage(data.message);
      } else {
        setNotificationMessage(
          `${item.productName} has been added to your cart!`
        );
      }

      setCartItems(prevItems => [...prevItems, item]);
      setWishlistItems(prevItems =>
        prevItems.filter(product => product.productId !== item.productId)
      );
      setWishlistUpdated(true);
      notifyCartChanged();
    } catch (error: unknown) {
      console.error('Error adding to cart:', error);
      setNotificationMessage('Failed to add item to cart.');
    }
  };

  const removeFromWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;
    if (!cartId) return;

    const ok = await confirm({
      title: 'Remove from wishlist',
      message: `Remove "${item.productName}" from wishlist?`,
      confirmText: 'Remove',
      cancelText: 'Cancel',
      variant: 'danger',
    });

    if (!ok) return;

    try {
      await axiosInstance.delete(
        `/carts/${cartId}/wishlist/${item.productId}`,
        { useV2: false }
      );

      setWishlistItems(prev =>
        prev.filter(p => p.productId !== item.productId)
      );
    } catch (e) {
      console.error(e);
      setNotificationMessage('Could not remove item from wishlist.');
    }
  };

  const moveAllWishlistToCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;
    if (!cartId || wishlistItems.length === 0) return;

    const ok = await confirm({
      title: 'Move all from wishlist',
      message: `Move ${wishlistItems.length} item(s) to your cart?`,
      confirmText: 'Move all',
      cancelText: 'Cancel',
    });
    if (!ok) return;

    setMovingAll(true);
    setNotificationMessage(null);

    try {
      const res = await axiosInstance.post(
        `/carts/${cartId}/wishlist/moveAll`,
        {},
        { useV2: true, validateStatus: () => true }
      );

      if (res.status >= 200 && res.status < 300) {
        setWishlistUpdated(true);
        notifyCartChanged();
        setNotificationMessage(null);
      } else {
        const msg =
          (res.data &&
            (res.data.message || res.data.error || res.data.title)) ||
          `Move All failed (${res.status})`;
        setNotificationMessage(msg);
      }
    } catch (e) {
      console.error(e);
      setNotificationMessage('Unexpected error while moving wishlist items.');
    } finally {
      setMovingAll(false);
    }
  };

  useEffect(() => {
    const saved = localStorage.getItem('invoices');
    if (saved) setInvoices(JSON.parse(saved));
  }, []);
  useEffect(() => {
    localStorage.setItem('invoices', JSON.stringify(invoices));
  }, [invoices]);

  const handleCheckoutConfirmation = (): void => {
    if (isStaff) {
      navigate(AppRoutePaths.Unauthorized, {
        state: { message: 'Staff/Admin cannot perform checkout.' },
      });
      return;
    }
    setShowBillingForm(true);
    setIsCheckoutModalOpen(false);
  };

  const handleCheckout = async (): Promise<void> => {
    if (isStaff) return;

    if (!cartId) {
      setCheckoutMessage('Invalid cart ID');
      return;
    }

    try {
      await axiosInstance.post(
        `/carts/${cartId}/checkout`,
        {},
        { useV2: false }
      );

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

  if (loading) return <div className="loading">Loading cart items...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div>
      <NavBar />

      <ConfirmModal />

      <h2 className="cart-header-title">Your Cart</h2>

      <div className="UserCart-container">
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

        <div className="UserCart-checkout-flex">
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

            <div className="UserCart-buttons">
              <button
                className="continue-shopping-btn"
                onClick={() => navigate('/products')}
              >
                Continue Shopping
              </button>
              <button
                className="clear-cart-btn"
                onClick={clearCart}
                disabled={isStaff}
                title={
                  isStaff
                    ? 'Read-only: staff/admin cannot clear carts'
                    : undefined
                }
              >
                Clear Cart
              </button>
            </div>
          </div>

          {!isStaff && (
            <div className="Checkout-section">
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

              {checkoutMessage && (
                <div className="checkout-message">{checkoutMessage}</div>
              )}

              {invoices.length > 0 && (
                <div className="invoices-section">
                  <h2>Invoice</h2>
                  <div className="invoice-summary">
                    {billingInfo && (
                      <div className="invoice-client-info">
                        <h3>Client Information</h3>
                        <p>
                          <strong>Name:</strong> {billingInfo.fullName}
                        </p>
                        <p>
                          <strong>Email:</strong> {billingInfo.email}
                        </p>
                        <p>
                          <strong>Address:</strong> {billingInfo.address},{' '}
                          {billingInfo.city}, {billingInfo.province},{' '}
                          {billingInfo.postalCode}
                        </p>
                      </div>
                    )}
                    {checkoutDate && (
                      <div className="invoice-date">
                        <strong>Checkout Date/Time:</strong> {checkoutDate}
                      </div>
                    )}
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
                    <div className="invoice-taxes">
                      {(() => {
                        const invoiceSubtotal = invoices.reduce(
                          (sum, inv) =>
                            sum + inv.productSalePrice * inv.quantity,
                          0
                        );
                        const invoiceTvq = invoiceSubtotal * 0.09975;
                        const invoiceTvc = invoiceSubtotal * 0.05;
                        const invoiceTotal =
                          invoiceSubtotal + invoiceTvq + invoiceTvc - discount;
                        return (
                          <>
                            <p>Subtotal: ${invoiceSubtotal.toFixed(2)}</p>
                            <p>TVQ (9.975%): ${invoiceTvq.toFixed(2)}</p>
                            <p>TVC (5%): ${invoiceTvc.toFixed(2)}</p>
                            <p>Discount: -${discount.toFixed(2)}</p>
                            <h3>Total: ${invoiceTotal.toFixed(2)}</h3>
                          </>
                        );
                      })()}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="wishlist-section">
          {/* Header with Move All button */}
          <div
            className="wishlist-header"
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <h2 style={{ margin: 0 }}>Your Wishlist</h2>
            {wishlistItems.length > 0 && (
              <button
                className="move-all-to-cart-btn"
                onClick={moveAllWishlistToCart}
                disabled={isStaff || movingAll}
                aria-busy={movingAll}
                aria-disabled={isStaff || movingAll}
                title={
                  isStaff
                    ? 'Read-only: staff/admin cannot move wishlist items'
                    : 'Move all wishlist items to cart'
                }
              >
                {movingAll ? 'Moving…' : 'Move All to Cart'}
              </button>
            )}
          </div>

          {/* Wishlist items */}
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

        {/* Billing Form Modal */}
        {showBillingForm && (
          <div className="modal-backdrop">
            <div className="modal-content">
              <CartBillingForm
                isOpen={true}
                onClose={() => setShowBillingForm(false)}
                onSubmit={async billing => {
                  setBillingInfo(billing);
                  setCheckoutDate(new Date().toLocaleString());
                  await handleCheckout();
                  setShowBillingForm(false);
                }}
              />
            </div>
          </div>
        )}

        {isCheckoutModalOpen && (
          <div className="checkout-modal">
            <h3>Confirm Checkout</h3>
            <p>Are you sure you want to checkout?</p>
            <button onClick={handleCheckout}>Yes</button>
            <button onClick={() => setIsCheckoutModalOpen(false)}>No</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserCart;
