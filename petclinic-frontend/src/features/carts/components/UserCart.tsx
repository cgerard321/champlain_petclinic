import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa';

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
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [wishlistUpdated, setWishlistUpdated] = useState(false);
  const [voucherCode, setVoucherCode] = useState<string>('');
  const [discount, setDiscount] = useState<number>(0);
  const [voucherError, setVoucherError] = useState<string | null>(null);

  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );

  const tvq = subtotal * 0.09975;
  const tvc = subtotal * 0.05;
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
    setWishlistUpdated(false);
    // Recalculate cart item count after setting cart items
    updateCartItemCount();
  }, [cartId, updateCartItemCount, wishlistUpdated]);

  useEffect(() => {
    const count = cartItems.reduce(
      (acc, item) => acc + (item.quantity || 0),
      0
    );
    setCartItemCount(count);
  }, [cartItems]);

  const applyVoucherCode = async (): Promise<void> => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/promos/validate/${voucherCode}`,
        {
          method: 'GET',
          headers: {
            Accept: 'application/json',
          },
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
      setVoucherError('Error validating voucher code:');
    }
  };

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
      } catch (error) {
        console.error('Error deleting item: ', error);
        alert('Failed to delete item');
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
          alert('Failed to clear cart');
        }
      } catch (error) {
        console.error('Error clearing cart:', error);
        alert('Failed to clear cart');
      }
    }
  };

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

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to add to wishlist');
      }

      setWishlistItems(prevItems => [...prevItems, item]);
      alert(`${item.productName} has been added to your wishlist!`);
      setWishlistUpdated(true);
    } catch (error) {
      console.error('Error adding to wishlist:', error);
      alert('Failed to add item to wishlist.');
    }
  };

  const addToCart = async (item: ProductModel): Promise<void> => {
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

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to add to cart');
      }

      setWishlistItems(prevItems => [...prevItems, item]);
      alert(`${item.productName} has been added to your cart!`);
      setWishlistUpdated(true);
    } catch (error) {
      console.error('Error adding to cart:', error);
      alert('Failed to add item to cart.');
    }
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
        const newInvoice: Invoice = {
          invoiceId: 'INV-' + new Date().getTime(),
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
          issueDate: new Date().toISOString(),
        };

        setInvoice(newInvoice);
        setCheckoutMessage('Checkout successful!');
        setCartItems([]);
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

      <div className="content-container">
        <div className="UserCart-checkout-flex">
          <div className="UserCart">
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
                  />
                ))
              ) : (
                <p className="empty-cart-message">No products in the cart.</p>
              )}
            </div>

            <div className="cart-control-buttons">
              <button className="btn go-back-btn" onClick={() => navigate(-1)}>
                Go Back
              </button>
              <button className="btn clear-cart-btn" onClick={clearCart}>
                Clear Cart
              </button>
            </div>

            <div
              className="voucher-code-section"
              style={{
                display: 'flex',
                alignItems: 'center',
                marginTop: '20px',
                flexDirection: 'column',
              }}
            >
              <div
                style={{ display: 'flex', alignItems: 'center', width: '100%' }}
              >
                <input
                  type="text"
                  placeholder="Enter voucher code"
                  value={voucherCode}
                  onChange={e => {
                    setVoucherCode(e.target.value);
                    setVoucherError(null);
                  }}
                  className="voucher-input"
                  style={{
                    width: '150px',
                    padding: '8px 12px',
                    border: '1px solid #ccc',
                    borderRadius: '4px',
                    marginRight: '10px',
                    fontSize: '14px',
                  }}
                />
                <button
                  onClick={applyVoucherCode}
                  className="apply-voucher-button"
                  style={{
                    padding: '8px 16px',
                    backgroundColor: '#28a745',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '14px',
                  }}
                >
                  Apply Voucher
                </button>
              </div>
              {voucherError && (
                <div className="voucher-error" role="alert">
                  {voucherError}
                </div>
              )}
            </div>

            <hr />

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

            <h3>Checkout</h3>
            <button className="btn checkout-btn" onClick={handleCheckout}>
              Checkout
            </button>
            {checkoutMessage && (
              <div className="checkout-message">{checkoutMessage}</div>
            )}

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
                    addToCart={addToCart}
                    isInWishlist={true}
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
