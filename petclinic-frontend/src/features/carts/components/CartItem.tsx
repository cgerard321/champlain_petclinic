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
    const remainingStock = item.productQuantity - (item.quantity ?? 0);

    return (
        <div className="CartItem">
            <h4>{item.productName}</h4>
            <p>{item.productDescription}</p>
            <div className="CartItem-details">
                <span>{formatPrice(item.productSalePrice)}</span>
                <input
                    type="number"
                    min="1"
                    max={item.productQuantity}
                    value={item.quantity ?? 0}
                    onChange={e => changeItemQuantity(e, index)}
                />
                <button onClick={() => deleteItem(index)}>Remove</button>
            </div>
            {remainingStock <= 5 && remainingStock > 0 ? (
                <div className="stock-message">
                    Only {remainingStock} items left in stock.
                </div>
            ) : remainingStock === 0 ? (
                <div className="stock-message out-of-stock">
                    Out of stock
                </div>
            ) : null}
        </div>
    );
};

export default CartItem;
