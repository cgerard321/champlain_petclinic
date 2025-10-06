// src/features/carts/components/CartItem.tsx
import './CartItem.css';
import { ProductModel } from '../models/ProductModel';
import ImageContainer from '@/features/products/components/ImageContainer';
import { useUser } from '@/context/UserContext';

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

const formatPrice = (price: number): string =>
  `$${price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;

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
      <ImageContainer imageId={item.imageId} />
      <div className="CartItem-info">
        <h2 className="info-title">{item.productName}</h2>
        <p className="info-description">{item.productDescription}</p>
      </div>

      <div className="CartItem-details">
        {/* Ligne panier normale */}
        {!isInWishlist && (
          <>
            <div className="item-quantity">
              <input
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
            <span className="CartItem-price">
              {formatPrice(item.productSalePrice)}
            </span>
            <button
              className="wishlist-button"
              onClick={() => deleteItem(item.productId, index)}
              aria-label={`Remove ${item.productName} from cart`}
              disabled={isStaff}
            >
              Remove
            </button>
            <button
              className="wishlist-button"
              onClick={() => addToWishlist(item)}
              aria-label={`Add ${item.productName} to wishlist`}
              disabled={isStaff}
            >
              Add to Wishlist
            </button>
          </>
        )}

        {/* Ligne wishlist */}
        {isInWishlist && (
          <div className="cartitem-actions">
            <button
              className="addToCart-button"
              onClick={handleAddToCart}
              aria-label={`Add ${item.productName} to cart`}
              disabled={isStaff || item.productQuantity === 0}
              aria-disabled={isStaff || item.productQuantity === 0}
            >
              {/* read-only: staff/admin OR out of stock */}
              {item.productQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
            </button>

            <button
              className="wishlist-button danger"
              style={{ marginLeft: '0.5rem' }}
              onClick={() => removeFromWishlist && removeFromWishlist(item)}
              aria-label={`Remove ${item.productName} from wishlist`}
              disabled={isStaff}
            >
              Remove
            </button>
          </div>
        )}
      </div>

      <div className="stock-message-container">
        {remainingStock <= 5 && remainingStock > 0 ? (
          <div className="stock-message">
            Only {remainingStock} items left in stock.
          </div>
        ) : remainingStock === 0 ? (
          <div className="stock-message out-of-stock">Out of stock</div>
        ) : null}
      </div>

      {errorMessage && <div className="item-error-message">{errorMessage}</div>}
    </div>
  );
};

export default CartItem;
