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
}

const formatPrice = (price: number): string => {
  return `$${price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
};

const CartItem = ({
  item,
  index,
  changeItemQuantity,
  deleteItem,
}: CartItemProps): JSX.Element => {
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
            value={item.quantity || 1}
            onChange={e => changeItemQuantity(e, index)}
            onBlur={e => changeItemQuantity(e, index)} // Confirm quantity change on blur
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
      </div>
    </div>
  );
};

export default CartItem;
