// CartItem.tsx
import './CartItem.css';
import { ProductModel } from '../models/ProductModel';
import ImageContainer from '@/features/products/components/ImageContainer';

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
  showNotification?: (message: string) => void; // New prop for notifications
}

const formatPrice = (price: number): string => {
  return `$${price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
};

const CartItem = ({
  item,
  index,
  changeItemQuantity,
  deleteItem,
  errorMessage, // Destructure the new prop
  addToWishlist,
  addToCart,
  isInWishlist,
  showNotification, // Destructure the new prop
}: CartItemProps): JSX.Element => {
  const remainingStock = item.productQuantity - (item.quantity ?? 0);

  // Handler for "Add to Cart" button click
  const handleAddToCart = (): void => {
    // Added return type `void`
    if (item.productQuantity > 0) {
      addToCart(item);
    } else {
      if (showNotification) {
        showNotification(`${item.productName} is out of stock.`);
      } else {
        alert(`${item.productName} is out of stock.`);
      }
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
              />
            </div>
            <span className="CartItem-price">
              {formatPrice(item.productSalePrice)}
            </span>
            <button
              className="wishlist-button"
              onClick={() => deleteItem(item.productId, index)}
              aria-label={`Remove ${item.productName} from cart`}
            >
              Remove
            </button>
            {!isInWishlist && (
              <button
                className="wishlist-button"
                onClick={() => addToWishlist(item)}
                aria-label={`Add ${item.productName} to wishlist`}
              >
                Add to Wishlist
              </button>
            )}
          </>
        )}

        {/* "Add to Cart" button for Wishlist Items */}
        {isInWishlist && (
          <button
            className="addToCart-button"
            onClick={handleAddToCart} // Use the new handler
            aria-label={`Add ${item.productName} to cart`}
          >
            Add to Cart
          </button>
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

      {/* Display per-item error message if exists */}
      {errorMessage && <div className="item-error-message">{errorMessage}</div>}
    </div>
  );
};

export default CartItem;
