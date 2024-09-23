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
      <h4>{item.productName}</h4>
      <p>{item.productDescription}</p>
      <div className="CartItem-details">
        <span>{formatPrice(item.productSalePrice)}</span>
        <input
          type="number"
          min="1"
          value={item.quantity || 1}
          onChange={e => changeItemQuantity(e, index)}
        />
        <button onClick={() => deleteItem(index)}>Remove</button>
      </div>
    </div>
  );
};

export default CartItem;
