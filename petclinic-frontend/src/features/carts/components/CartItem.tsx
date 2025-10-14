// src/features/carts/components/CartItem.tsx
import './cart-shared.css';
import './CartItem.css';
import { ProductModel } from '../models/ProductModel';
import ImageContainer from '@/features/products/components/ImageContainer';
import { useUser } from '@/context/UserContext';
import { formatPrice } from '../utils/formatPrice';

interface CartItemProps {
  item: ProductModel;
  index: number;
  changeItemQuantity: (
    event: React.ChangeEvent<HTMLInputElement>,
    index: number
  ) => void;
  deleteItem: (productId: string, indexToDelete: number) => void;
  errorMessage?: string;
  addToWishlist: (item: ProductModel) => void;
  addToCart: (item: ProductModel) => void;
  isInWishlist: boolean;
  showNotification?: (message: string) => void;
  removeFromWishlist?: (item: ProductModel) => void;
}

const CartItem = ({
  item,
  index,
  changeItemQuantity,
  deleteItem,
  errorMessage,
  addToWishlist,
  addToCart,
  isInWishlist,
  showNotification,
  removeFromWishlist,
}: CartItemProps): JSX.Element => {
  const remainingStock = item.productQuantity - (item.quantity ?? 0);

  // ---- rôles / read-only staff+admin ----
  const { user } = useUser();
  const roleNames = new Set<string>();
  const rolesSet = user?.roles;
  if (rolesSet) for (const r of rolesSet) roleNames.add(r.name);

  const isAdmin = roleNames.has('ADMIN');
  const isStaff =
    isAdmin ||
    roleNames.has('EMPLOYEE') ||
    roleNames.has('VET') ||
    roleNames.has('INVENTORY_MANAGER') ||
    roleNames.has('RECEPTIONIST');

  // Add to cart (depuis la wishlist)
  const handleAddToCart = (): void => {
    if (isStaff) return; // lecture seule
    if (item.productQuantity > 0) {
      addToCart(item);
    } else {
      if (showNotification)
        showNotification(`${item.productName} is out of stock.`);
      else alert(`${item.productName} is out of stock.`);
    }
  };

  return (
    <div className="CartItem">
      <div className="cart-item-media">
        <ImageContainer imageId={item.imageId} />
      </div>

      <div className="cart-item-content">
        <div className="cart-item-header">
          <h2 className="info-title">{item.productName}</h2>
          {remainingStock <= 5 && remainingStock > 0 && (
            <span className="stock-message">
              Only {remainingStock} items left in stock.
            </span>
          )}
          {remainingStock === 0 && (
            <span className="stock-message out-of-stock">Out of stock</span>
          )}
        </div>

        <p className="info-description">{item.productDescription}</p>

        {/* Ligne panier normale */}
        {!isInWishlist && (
          <div className="cart-item-meta">
            <div className="item-controls">
              <div className="item-quantity">
                <label
                  className="sr-only"
                  htmlFor={`quantity-${item.productId}`}
                >
                  Quantity of {item.productName}
                </label>
                <input
                  id={`quantity-${item.productId}`}
                  type="number"
                  min="1"
                  max={item.productQuantity}
                  value={item.quantity || 1}
                  onChange={e => changeItemQuantity(e, index)}
                  onBlur={e => changeItemQuantity(e, index)}
                  aria-label={`Quantity of ${item.productName}`}
                  disabled={isStaff}
                />
              </div>
              <div className="item-actions-inline">
                <button
                  type="button"
                  className="action-link danger"
                  onClick={() => deleteItem(item.productId, index)}
                  disabled={isStaff}
                >
                  Remove
                </button>
                <span className="action-divider" aria-hidden="true" />
                <button
                  type="button"
                  className="action-link"
                  onClick={() => addToWishlist(item)}
                  disabled={isStaff}
                >
                  Add to wishlist
                </button>
              </div>
            </div>
            <span className="CartItem-price">
              {formatPrice(item.productSalePrice)}
            </span>
          </div>
        )}

        {/* Ligne wishlist */}
        {isInWishlist && (
          <div className="cart-item-meta wishlist">
            <div className="cart-item-actions">
              <button
                className="cart-button cart-button--accent cart-button--pill cart-button--block cart-button--strike-disabled"
                onClick={handleAddToCart}
                aria-label={`Add ${item.productName} to cart`}
                disabled={isStaff || item.productQuantity === 0}
                aria-disabled={isStaff || item.productQuantity === 0}
              >
                {/* read-only: staff/admin OR out of stock */}
                {item.productQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
              </button>

              <button
                className="cart-button cart-button--danger cart-button--pill cart-button--block cart-button--strike-disabled"
                onClick={() => removeFromWishlist && removeFromWishlist(item)}
                aria-label={`Remove ${item.productName} from wishlist`}
                disabled={isStaff}
              >
                Remove
              </button>
            </div>
          </div>
        )}

        {errorMessage && (
          <div className="item-error-message">{errorMessage}</div>
        )}
      </div>
    </div>
  );
};

export default CartItem;
