import './CartItem.css';
import { ProductModel } from '../models/ProductModel';

interface CartItemProps {
  item: ProductModel;
  index: number;
  changeItemQuantity: (
    event: React.ChangeEvent<HTMLInputElement>,
    index: number
  ) => void;
  deleteItem: (indexToDelete: number) => void;
  errorMessage?: string;
  // addToWishlist: (item: ProductModel) => void;
}

const formatPrice = (price: number): string => {
  return `$${price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
};

const CartItem = ({
  item,
  index,
  changeItemQuantity,
  deleteItem,
  // addToWishlist,
}: CartItemProps): JSX.Element => {
  const remainingStock = item.productQuantity - (item.quantity ?? 0);

  return (
    <div className="CartItem">
      <div className="CartItem-info">
        <h2 className="info-title">{item.productName}</h2>
        <p className="info-description">{item.productDescription}</p>
      </div>
      <div className="CartItem-details">
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
          className="delete-button"
          onClick={() => deleteItem(index)}
          aria-label={`Remove ${item.productName} from cart`}
        >
          Remove
        </button>
        {/* <button 
          className="wishlist-button" // Add a class for styling
          onClick={() => addToWishlist(item)} // Call the addToWishlist function
          aria-label={`Add ${item.productName} to wishlist`}
        >
          Add to Wishlist
        </button> */}
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
    </div>
  );
};

export default CartItem;
